package backend

import arrow.core.Either
import arrow.resilience.Schedule

class PriceFetcher(
    private val stackerBackend: StackerBackend,
) {
    suspend fun priceOf(token: String): Float? {
        stackerBackend.getPriceOfToken(token)
        return Schedule.doUntil<Either<Throwable, Float>> { input, _ ->
            input is Either.Right<Float>
        }.repeat {
            stackerBackend.getPriceOfToken(token)
        }.getOrNull()
    }
}