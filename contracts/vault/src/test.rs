#![cfg(test)]
extern crate std;
use soroban_sdk::{symbol_short, testutils::Address as _, vec, xdr::ScVal, Address, Env, IntoVal, TryFromVal, TryIntoVal, Val};

#[test]
fn test() {
    let env = Env::default();

    let binding = Address::generate(&env);
    // let val = binding.as_val();

    let val: Val = ScVal::Void.into_val(&env);

    let addr = Address::try_from_val(&env, &val).ok();
    // std::dbg!();

}
