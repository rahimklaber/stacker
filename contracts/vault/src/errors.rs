use soroban_sdk::{contracterror, panic_with_error, Env};

#[contracterror]
#[derive(Copy, Clone)]
#[repr(u32)]
pub enum Error{
    NotGreaterThan0 = 0,
    TooMuchLostWithDeposit = 1,
}

pub fn require_gt_0(e: &Env, value: i128){
    if value < 0 {
        panic_with_error!(&e, Error::NotGreaterThan0)
    }
}