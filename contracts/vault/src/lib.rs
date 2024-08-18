#![no_std]
use crate::errors::require_gt_0;
use crate::storage::{get_deposit_token, get_tot_supply, update_shares};
use soroban_sdk::token::TokenClient;
use soroban_sdk::{Address, Env};

pub trait VaultTrait {
    // fn init(e: Env, deposit_token: Address);
    /// returns amount of shares
    fn deposit(e: Env, depositor: Address, amount: i128) -> i128;
    /// get amount of shares
    fn balance(e: Env, depositor: Address) -> i128;
    // returns amount of tokens received
    fn withdraw(e: Env, depositor: Address, amount: i128);
}

pub mod vault_trait_default {
    use crate::errors::{require_gt_0, Error};
    use crate::preview_withdraw;
    use crate::storage::{DataKey, _get_deposit_token, get_deposit_token, get_leeway, get_shares, get_tot_supply, update_shares};
    use soroban_sdk::token::TokenClient;
    use soroban_sdk::{assert_with_error, Address, Env};
    use crate::events::{publish_deposit, publish_withdraw};

    pub fn init(e: Env, deposit_token: Address, lee_way: i128) {
        if let None = _get_deposit_token(&e) {
            e.storage()
                .instance()
                .set(&DataKey::DepositToken, &deposit_token);

            e.storage()
            .instance()
            .set(&DataKey::LeeWay, &lee_way);
        }
    }

    pub fn deposit(e: Env, depositor: Address, amount: i128) -> i128 {
        depositor.require_auth();
        require_gt_0(&e, amount);

        let deposit_token = get_deposit_token(&e);

        let tot_supply_shares = get_tot_supply(&e);

        let token_client = TokenClient::new(&e, &deposit_token);


        let shares_to_mint = if tot_supply_shares == 0 {
            amount
        } else {
            let token_balance = token_client.balance(&e.current_contract_address());

            let shares = (amount * tot_supply_shares) / token_balance;

            //todo does this work?
            assert_with_error!(&e, amount - preview_withdraw(shares, token_balance + amount, tot_supply_shares) < get_leeway(&e), Error::TooMuchLostWithDeposit);

            shares
        };


        token_client.transfer(&depositor, &e.current_contract_address(), &amount);

        publish_deposit(&e, &depositor, amount);

        update_shares(&e, &depositor, shares_to_mint)
    }

    pub fn balance(e: Env, depositor: Address) -> i128 {
        get_shares(&e, depositor)
    }

    pub fn withdraw(e: Env, depositor: Address, amount_shares: i128) {
        depositor.require_auth();
        require_gt_0(&e, amount_shares);

        let deposit_token = get_deposit_token(&e);
        let token_client = TokenClient::new(&e, &deposit_token);

        let token_balance = token_client.balance(&e.current_contract_address());
        let tot_supply_shares = get_tot_supply(&e);

        let amount_tokens = (amount_shares * token_balance) / tot_supply_shares;

        publish_withdraw(&e, &depositor, amount_shares);

        update_shares(&e, &depositor, -amount_shares);
        token_client.transfer(&e.current_contract_address(), &depositor, &amount_tokens);
    }
}

fn preview_withdraw(amount_shares: i128, token_balance: i128, tot_supply: i128) -> i128{
    return(amount_shares * token_balance) / tot_supply;
}

// pub fn deposit_for_fee(e: &Env, depositor: Address, amount: i128) -> i128 {
//     require_gt_0(&e, amount);

//     let deposit_token = get_deposit_token(&e);

//     let tot_supply_shares = get_tot_supply(&e);

//     let token_client = TokenClient::new(&e, &deposit_token);

//     let shares_to_mint = if tot_supply_shares == 0 {
//         amount
//     } else {
//         // We need to adjust the token balance for the calculation, since the contract already holds the tokens.
//         (amount * tot_supply_shares) / (token_client.balance(&e.current_contract_address()) - amount)
//     };

//     update_shares(&e, depositor.clone(), shares_to_mint)
// }

// #[contract]
// pub struct Vault;
//
// #[contractimpl]
// impl VaultTrait for Vault {
//     fn init(e: Env, deposit_token: Address) {
//         if let None = _get_deposit_token(&e) {
//             e.storage()
//                 .instance()
//                 .set(&DataKey::DepositToken, &deposit_token)
//         }
//     }
//
//     fn deposit(e: Env, depositor: Address, amount: i128) -> i128 {
//         depositor.require_auth();
//         require_gt_0(&e, amount);
//
//         let deposit_token = get_deposit_token(&e);
//
//         let tot_supply_shares = get_tot_supply(&e);
//
//         let token_client = TokenClient::new(&e, &deposit_token);
//
//         let shares_to_mint = if (tot_supply_shares == 0) {
//             amount
//         } else {
//             (amount * tot_supply_shares) / token_client.balance(&e.current_contract_address())
//         };
//
//         token_client.transfer(&depositor, &e.current_contract_address(), &amount);
//
//         update_shares(&e, depositor.clone(), shares_to_mint)
//     }
//
//     fn get_balance(e: Env, depositor: Address) -> i128 {
//         get_shares(&e, depositor)
//     }
//
//     fn withdraw(e: Env, depositor: Address, amount_shares: i128) -> i128 {
//         require_gt_0(&e, amount_shares);
//
//         let deposit_token = get_deposit_token(&e);
//         let token_client = TokenClient::new(&e, &deposit_token);
//
//         let token_balance = token_client.balance(&e.current_contract_address());
//         let tot_supply_shares = get_tot_supply(&e);
//
//         let amount_tokens = (amount_shares * token_balance) / tot_supply_shares;
//
//         update_shares(&e, depositor.clone(), -amount_shares);
//         token_client.transfer(&depositor, &e.current_contract_address(), &amount_tokens);
//
//         amount_tokens
//     }
// }

mod test;
mod storage;
mod errors;
mod events;
