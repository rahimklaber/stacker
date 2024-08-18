package vault


data class VaultData(
    val name: String,
    val vaultContract: String,
    val pairContract: String,
    val token: String,
    val tokens: List<String>,
    val feeBps: UInt,
    val rewardToken: String,
    val exchange: String,
    val linkToProtocol: String,
    val description: String = "",
)

val DEFAULT_VAULTS = listOf(
    VaultData(
        "AQUA/USDC Aquarius pool",
        "CDSTJSU5K6F5VYITYVID27VZCYFLX72ZD7SU3XPLGNGEH2KKLDQVJ3O6",
        "CA6GAFOJCW4MGQQBUCQUSA3CLIH25G4SNKB2JHYKZCVWZTNW5VXMSC4O",
        "CDXNFQFXBB3QO5LC37EUOXX2J2WSSRHONGDTPBOOPGLS3GJXI5Y3SFFD",
        listOf(
            "CCW67TSZV3SSS2HXMBQ5JFGCKJNXKZM7UQUWUZPUTHXSTZLEO7SJMI75",
            "CAUIKL3IYGMERDRUN6YSCLWVAKIFG5Q4YJHUKM4S4NJZQIA3BAS6OJPK"
        ),
        250u,
        "CAUIKL3IYGMERDRUN6YSCLWVAKIFG5Q4YJHUKM4S4NJZQIA3BAS6OJPK",
        "Aquarius",
        linkToProtocol = "https://aqua.network/pools/CA6GAFOJCW4MGQQBUCQUSA3CLIH25G4SNKB2JHYKZCVWZTNW5VXMSC4O/",
        description = """
            This strategy is as follows: 
            1. Claim Aqua rewards accumulated due to locked lp shares
            2. Sell the aqua rewards and re-invest the proceeds by depositing in the liquidity pool.
            
            Note: The keeper tries to do this daily. However, it will take longer if the vault fee does not cover the transaction fee.
        """.trimIndent()
    ),
)
