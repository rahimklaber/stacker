#![cfg(test)]
extern crate std;

use soroban_sdk::{contract, contractimpl, testutils::Address as _, token::StellarAssetClient, Address, Env};

use crate::{vault_trait_default, VaultTrait};

#[contract]
struct VaultTestContract;

#[contractimpl]
impl VaultTestContract {
    pub fn init(e: Env, deposit_token: Address, lee_way: i128){
        vault_trait_default::init(e.clone(), deposit_token, lee_way);
    }
}

#[contractimpl]
impl VaultTrait for VaultTestContract {
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


fn create_token_contract<'a>(e: &Env, admin: &Address) -> StellarAssetClient<'a> {
    StellarAssetClient::new(e, &e.register_stellar_asset_contract(admin.clone()))
}

#[test]
fn test() {
    let env = Env::default();
    env.mock_all_auths();

    let token_admin = Address::generate(&env);
    let addr_1 = Address::generate(&env);
    let addr_2 = Address::generate(&env);
    
    let token_client = create_token_contract(&env, &token_admin);
    
    token_client.mint(&addr_1, &10_000_000_000_0);
    token_client.mint(&addr_1, &10_000_000_000_0);


    let vault_contract = env.register_contract(None, VaultTestContract{});
    let vault_client = VaultTestContractClient::new(&env, &vault_contract);
    vault_client.init(&token_client.address, &1_000_000_0);

    let deposit_amount = 10_000_000_0;
    
    let shares = vault_client.deposit(&addr_1, &deposit_amount);

    token_client.mint(&vault_contract, &10_000_000_000_000);

    let amount_withdrawn = vault_client.withdraw(&addr_1, &shares);

    assert_eq!(10_000_000_000_000 + deposit_amount, amount_withdrawn);
}

#[test]
fn testBlockSmallDeposit() {
    let env = Env::default();
    env.mock_all_auths();

    let token_admin = Address::generate(&env);
    let addr_1 = Address::generate(&env);
    
    let token_client = create_token_contract(&env, &token_admin);
    
    token_client.mint(&addr_1, &10_000_000_000_0);


    let vault_contract = env.register_contract(None, VaultTestContract{});
    let vault_client = VaultTestContractClient::new(&env, &vault_contract);
    vault_client.init(&token_client.address, &1_000_000_0);

    token_client.mint(&vault_contract, &10_000_000_000_000);
    
    // first deposit will always succeed
    vault_client.try_deposit(&addr_1, &10000);
    let shares = vault_client.try_deposit(&addr_1, &10000);

    assert!(shares.is_err());
}
