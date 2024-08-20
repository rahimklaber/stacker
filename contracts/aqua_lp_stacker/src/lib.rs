#![no_std]

use soroban_fixed_point_math::{SorobanFixedPoint};
use soroban_sdk::auth::{ContractContext, InvokerContractAuthEntry, SubContractInvocation};
use soroban_sdk::token::TokenClient;
use soroban_sdk::{contract, contracterror, contractimpl, contracttype, panic_with_error, vec, Address, BytesN, Env, IntoVal, Symbol, TryFromVal, TryIntoVal, Val};
use vault::VaultTrait;
use vault::{vault_trait_default};
use crate::events::publish_keeper;

#[contracttype]
pub enum DataKey {
    Config
}
#[contracttype]
pub enum OptionalAddress {
    None,
    Some(Address),
}

#[contracttype]
pub struct StackerConfig {
    fee_recipient: Address,
    fee_bps: u32, // fee in bps
    reward_token: Address,
    pair_sell_reward: Val, // aqua pair to sell reward (aqua) and get token_0 or token_1
    pair: Address, // aqua pair to sell token_1 and get token_0 to deposit in the lp. This is the pair we will deposit into
    // constant_product: bool, // if pool is constant product
}

pub fn _get_config(e: &Env) -> Option<StackerConfig> {
    e.storage()
        .instance()
        .get(&DataKey::Config)
}

pub fn get_config(e: &Env) -> StackerConfig {
    _get_config(e).unwrap()
}


mod aqua_amm {
    use soroban_sdk::contractimport;

    contractimport!(
        file = "B:/code_projects/Soroban Vault/contracts/aqua_pair.wasm"
    );
}

#[contracterror]
#[derive(Copy, Clone)]
#[repr(u32)]
pub enum Error {
    CannotSwapReward = 100,
    CannotQuoteDeposit = 101,
    NotSupportedYet = 202,
}

#[contracttype]
pub struct KeeperArgs {
    //args for the first swap
    in_idx_0: u32,
    out_amount_0: u128,

    //args for the second swap
    in_idx_1: u32,
    out_amount_1: u128,
    in_token_1: Val /* Address | VOID */
}

pub trait AquaStackerTrait {
    fn init(e: Env, config: StackerConfig, deposit_token: Address, lee_way: i128);

    fn keeper(e: Env, args: KeeperArgs) -> u128;

    fn claim_fee(e: Env, amount: i128);
}

#[contract]
pub struct AquaStacker;


#[contractimpl]
impl AquaStacker {
    pub fn upgrade(env: Env, new_wasm_hash: BytesN<32>) {
        get_config(&env).fee_recipient.require_auth();

        env.deployer().update_current_contract_wasm(new_wasm_hash);
    }
}


#[contractimpl]
impl AquaStackerTrait for AquaStacker {
    fn init(e: Env, config: StackerConfig, deposit_token: Address, lee_way: i128) {
        if let None = _get_config(&e) {
            e.storage()
                .instance()
                .set(&DataKey::Config, &config);

            vault_trait_default::init(e.clone(), deposit_token, lee_way);
            aqua_amm::Client::new(&e, &config.pair).get_rewards_info(&e.current_contract_address());
        }
    }

    fn keeper(e: Env, args: KeeperArgs) -> u128 {
        let config = get_config(&e);
        let pair_sell_reward = Address::try_from_val(&e, &config.pair_sell_reward).ok();

        let amm_client = aqua_amm::Client::new(&e, &config.pair);
        let amount_claimed = amm_client.claim(&e.current_contract_address());

        let reward_minus_fee = amount_claimed.fixed_mul_floor(&e, &(10_000 - u128::from(config.fee_bps)), &10_000);
        let fee_amount = amount_claimed - reward_minus_fee;

        let lp_shares = match pair_sell_reward {
            None => { // the reward token (aqua) is one of the amm tokens
                let amount_to_swap = reward_minus_fee / 2;
                let amount_left = reward_minus_fee - amount_to_swap;

                e.authorize_as_current_contract(vec![
                    &e,
                    InvokerContractAuthEntry::Contract(SubContractInvocation {
                        context: ContractContext {
                            contract: config.reward_token.clone(),
                            fn_name: Symbol::new(&e, "transfer"),
                            args: (e.current_contract_address(), config.pair.clone(), amount_to_swap as i128).into_val(&e),
                        },
                        // `sub_invocations` can be used to authorize even deeper
                        // calls.
                        sub_invocations: vec![&e],
                    }),
                ]);

                let out = amm_client.swap(&e.current_contract_address(), &args.in_idx_0, &(1 - args.in_idx_0), &amount_to_swap, &args.out_amount_0);

                let d_amounts = if args.in_idx_0 == 0 {
                    vec![&e, amount_left, out]
                } else {
                    vec![&e, out, amount_left]
                };

                let (auth_1, auth_2) = 
                    (
                        (config.reward_token.clone(), amount_left),
                        (Address::try_from_val(&e, &args.in_token_1).unwrap(), out),
                    );
                


                e.authorize_as_current_contract(vec![
                    &e,
                    InvokerContractAuthEntry::Contract(SubContractInvocation {
                        context: ContractContext {
                            contract: auth_1.0,
                            fn_name: Symbol::new(&e, "transfer"),
                            args: (e.current_contract_address(), config.pair.clone(), auth_1.1 as i128).into_val(&e),
                        },
                        // `sub_invocations` can be used to authorize even deeper
                        // calls.
                        sub_invocations: vec![&e],
                    }),
                ]);

                e.authorize_as_current_contract(vec![
                    &e,
                    InvokerContractAuthEntry::Contract(SubContractInvocation {
                        context: ContractContext {
                            contract: auth_2.0,
                            fn_name: Symbol::new(&e, "transfer"),
                            args: (e.current_contract_address(), config.pair.clone(), auth_2.1 as i128).into_val(&e),
                        },
                        // `sub_invocations` can be used to authorize even deeper
                        // calls.
                        sub_invocations: vec![&e],
                    }),
                ]);

                amm_client.deposit(&e.current_contract_address(), &d_amounts, &0).1
            }
            // this is could be better but 🤷‍♀️
            Some(sell_reward_amm) => {
                panic_with_error!(&e, Error::NotSupportedYet)
                // let sell_reward_client = aqua_amm::Client::new(&e, &sell_reward_amm);
                // let amount_to_swap = reward_minus_fee;

                // e.authorize_as_current_contract(vec![
                //     &e,
                //     InvokerContractAuthEntry::Contract(SubContractInvocation {
                //         context: ContractContext {
                //             contract: config.reward_token.clone(),
                //             fn_name: Symbol::new(&e, "transfer"),
                //             args: (e.current_contract_address(), sell_reward_amm, amount_to_swap as i128).into_val(&e),
                //         },
                //         // `sub_invocations` can be used to authorize even deeper
                //         // calls.
                //         sub_invocations: vec![&e],
                //     }),
                // ]);


                // // we got one of the tokens
                // let amount_out_first = sell_reward_client.swap(&e.current_contract_address(), &args.in_idx_0, &(1 - args.in_idx_0), &amount_to_swap, &args.out_amount_0);

                // let amount_in_second = amount_out_first / 2;
                // let amount_left = amount_out_first - amount_in_second;


                // e.authorize_as_current_contract(vec![
                //     &e,
                //     InvokerContractAuthEntry::Contract(SubContractInvocation {
                //         context: ContractContext {
                //             contract: args.in_token_1.try_into_val(&e).unwrap(),
                //             fn_name: Symbol::new(&e, "transfer"),
                //             args: (e.current_contract_address(), config.pair.clone(), amount_in_second as i128).into_val(&e),
                //         },
                //         // `sub_invocations` can be used to authorize even deeper
                //         // calls.
                //         sub_invocations: vec![&e],
                //     }),
                // ]);
                // // now sell those tokens for the other token in the pair, so we can deposit.
                // let amount_out_second = amm_client.swap(&e.current_contract_address(), &args.in_idx_1, &(1 - args.in_idx_1), &amount_in_second, &args.out_amount_1);

                // let d_amounts = if args.in_idx_1 == 0 {
                //     vec![&e, amount_left, amount_out_second]
                // } else {
                //     vec![&e, amount_out_second, amount_left]
                // };

                // amm_client.deposit(&e.current_contract_address(), &d_amounts, &0).1
            }
        };

        publish_keeper(&e, amount_claimed as i128, fee_amount as i128);

        fee_amount
    }


    fn claim_fee(e: Env, amount: i128){
        let config = get_config(&e);
        config.fee_recipient.require_auth();

        TokenClient::new(&e, &config.reward_token).transfer(&e.current_contract_address(), &config.fee_recipient, &amount);
    }
}

#[contractimpl]
impl VaultTrait for AquaStacker {
    fn deposit(e: Env, depositor: Address, amount: i128) -> i128 {
        vault_trait_default::deposit(e.clone(), depositor.clone(), amount)
    }

    fn balance(e: Env, depositor: Address) -> i128 {
        vault_trait_default::balance(e, depositor)
    }

    fn withdraw(e: Env, depositor: Address, amount: i128) -> i128 {
        vault_trait_default::withdraw(e.clone(), depositor.clone(), amount)
    }
}

mod test;
mod events;

