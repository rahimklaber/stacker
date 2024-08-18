#![cfg(test)]
extern crate std;

use soroban_sdk::{symbol_short, token, vec, Val};
use soroban_sdk::token::StellarAssetClient;
use soroban_sdk::xdr::{Limits, ReadXdr, ScMap, ScVal, VecM};
use soroban_sdk::{testutils::Address as _, Address, Env, IntoVal, Symbol};


fn create_token_contract<'a>(e: &Env, admin: &Address) -> StellarAssetClient<'a> {
    StellarAssetClient::new(e, &e.register_stellar_asset_contract(admin.clone()))
}

const WASM: &[u8] = include_bytes!("../../target/wasm32-unknown-unknown/release/aqua_lp_stacker.wasm");

#[test]
fn test() {
    let env = Env::default();
    env.mock_all_auths();


    // let contract_id = env.register_contract(None, AquaStacker);

    let contract_id = env.register_contract_wasm(None, WASM);

    // let client = AquaStackerClient::new(&env, &contract_id);

    let mut address1 = Address::generate(&env);
    let token_client = create_token_contract(&env, &address1);


    let map = ScVal::from_xdr_base64("AAAAEQAAAAEAAAAFAAAADwAAAAdmZWVfYnBzAAAAAAMAAAD6AAAADwAAAA1mZWVfcmVjaXBpZW50AAAAAAAAEgAAAAAAAAAAjt+1MeLWTb+P4K9mohQF8P+rnnUX33fLmaPsaaRJMB4AAAAPAAAABHBhaXIAAAASAAAAATxgFckVuMNCAaChSQNiWg+um5Jqg6SfCsirbM227W7JAAAADwAAABBwYWlyX3NlbGxfcmV3YXJkAAAAAQAAAA8AAAAMcmV3YXJkX3Rva2VuAAAAEgAAAAEohS9owZhIjjRvsSEu1QKQU3Ycwk9FM5LjU5ggGwgl5w==", Limits::none()).unwrap().into_val(&env);

    env.invoke_contract::<Val>(&contract_id, &symbol_short!("init"), vec![&env, map]);

    // client.init(&StackerConfig{
    //     fee_recipient: Address::generate(&env),
    //     fee_bps: 260,
    //     reward_token: Address::generate(&env),
    //     pair_sell_reward: ().into_val(&env),
    //     pair: Address::generate(&env)
    // },&token_client.address , &1000000);

    // token_client.mint(&address1, &10_000_000_0);

    // client.deposit(&address1, &5_000_000_0);

    // client.withdraw(&address1, &5_000_000_0);

    // client.keeper(&crate::KeeperArgs { in_idx_0: 0, out_amount_0: 0, in_idx_1: 0, out_amount_1: 0 });

}
