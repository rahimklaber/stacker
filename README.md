## Links
contract    : https://stellar.expert/explorer/public/contract/CDSTJSU5K6F5VYITYVID27VZCYFLX72ZD7SU3XPLGNGEH2KKLDQVJ3O6

mainnet dapp: https://stacker.rahimklaber.me/

## Contract



```rust
pub trait VaultTrait {
    /// returns amount of shares
    fn deposit(e: Env, depositor: Address, amount: i128) -> i128;
    /// get amount of shares
    fn balance(e: Env, depositor: Address) -> i128;
    // returns amount of tokens received
    fn withdraw(e: Env, depositor: Address, amount: i128);
}

pub trait AquaStackerTrait {
    fn init(e: Env, config: StackerConfig, deposit_token: Address, lee_way: i128);

    fn keeper(e: Env, args: KeeperArgs) -> u128;

    fn claim_fee(e: Env, amount: i128);
}
```

The contract contains 2 main functionalities. The vault and the stacker. The vault is responsible for allowing a user to deposit and withdraw their funds, while the stacker is responsible for re-invesing rewards.

The keeper function in particular is interesting:
```rust
    fn keeper(e: Env, args: KeeperArgs) -> u128 {
        let config = get_config(&e);

        let amm_client = aqua_amm::Client::new(&e, &config.pair);
        let amount_claimed = amm_client.claim(&e.current_contract_address()); <-- CLAIM REWARD

        let reward_minus_fee = amount_claimed.fixed_mul_floor(&e, &(10_000 - u128::from(config.fee_bps)), &10_000); <-- MAKE SURE TO TAKE VAULT FEE
        let fee_amount = amount_claimed - reward_minus_fee;

        let amount_to_swap = reward_minus_fee / 2;
        let amount_left = reward_minus_fee - amount_to_swap;

        ...
        let out = amm_client.swap(&e.current_contract_address(), &args.in_idx_0, &(1 - args.in_idx_0), &amount_to_swap, &args.out_amount_0); <-- SWAP HALF FOR USDC

        let d_amounts = if args.in_idx_0 == 0 {
            vec![&e, amount_left, out]
        } else {
            vec![&e, out, amount_left]
        };
        ...
        amm_client.deposit(&e.current_contract_address(), &d_amounts, &0).1 <-- DEPOSIT AQUA AND USDC TO GET MORE LP TOKENS
}
```

Some details are not shown, but the important parts are there.
The keeper function does the following:
1. It claims the aqua rewards from the Aquarius amm.
2. It sells half of the aqua for USDC.
3. It deposits the Aqua and USDC to receive more lp shares. 


## Development
1. clone https://github.com/rahimklaber/stellar_kt, cd into it and run `./gradlew publishAllPublicationsToMavenLocalRepository`. I use my own sdk which not published yet.
2. To start the frontend: clone this repo, cd into and run `./gradlew jsBrowserDevelopmentRun -t`. This will start the dev server with auto-reload when you make any changes.
3. The contracts live in the `contracts` folder. Refer to https://developers.stellar.org/docs/build for how to develop contracts.
