use soroban_sdk::{contracttype, Address, Env};
use crate::errors::require_gt_0;

#[contracttype]
pub enum DataKey{
    DepositToken,
    // Balance(Address),
    Shares(Address),
    TotSupply,
    LeeWay
}


pub(crate) fn _get_deposit_token(e: &Env) -> Option<Address>{
    e
        .storage()
        .instance()
        .get(&DataKey::DepositToken)
}

pub fn get_deposit_token(e: &Env) -> Address{
    _get_deposit_token(e).unwrap()
}

pub fn get_tot_supply(e: &Env) -> i128{
    e.storage()
        .instance()
        .get(&DataKey::TotSupply)
        .unwrap_or(0)
}

fn update_tot_supply(e: &Env, amount: i128){
    e.storage()
        .instance()
        .set(&DataKey::TotSupply, &(get_tot_supply(e) + amount))
}

pub fn get_shares(e: &Env, address: Address) -> i128{
    e.storage()
        .persistent()
        .get(&DataKey::Shares(address))
        .unwrap_or(0)
}

pub fn set_shares(e: &Env, depositor: Address, amount: i128){
    e.storage()
        .persistent()
        .set(&DataKey::Shares(depositor), &amount)
}

pub fn update_shares(e: &Env, depositor: &Address, amount: i128) -> i128{
    let new_amount = get_shares(e, depositor.clone()) + amount;
    require_gt_0(e, new_amount);

    e.storage()
        .persistent()
        .set(&DataKey::Shares(depositor.clone()), &new_amount);

    update_tot_supply(&e, amount);

    new_amount
}

pub fn get_leeway(e: &Env) -> i128{
    e.storage()
    .instance()
    .get(&DataKey::LeeWay).unwrap()
}