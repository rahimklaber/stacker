use soroban_sdk::{symbol_short, Address, Env};

pub fn publish_deposit(e: &Env, depositor: &Address, amount: i128){
    e
        .events()
        .publish((symbol_short!("deposit"), depositor), amount);
}

pub fn publish_withdraw(e: &Env, depositor: &Address, amount: i128){
    e
        .events()
        .publish((symbol_short!("withdraw"), depositor), amount);
}