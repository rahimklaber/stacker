use soroban_sdk::{symbol_short, Env};

pub fn publish_keeper(e: &Env, rewards: i128, fee_lp: i128) {
    e
        .events()
        .publish((symbol_short!("keeper"),), (rewards, fee_lp));
}