package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Service
import java.util.UUID

interface RegisterDataHandler {
    fun handle(soknadId: UUID)
}

@Service
class RegisterDataService(
    private val handlers: List<RegisterDataHandler>,
) {
    private val logger by logger()

    fun runAllRegisterDataHandlers(soknadId: UUID) {
        logger.info("NyModell: Henter Register-data")
        doRunListedHandlers(soknadId = soknadId, listedHandlers = handlers)
    }

    fun runSpecificHandlers(
        soknadId: UUID,
        listedHandlers: List<RegisterDataHandler>,
    ) {
        logger.info("NyModell: Henter Register-data: ${listedHandlers.joinToString(separator = ", ")}")
        doRunListedHandlers(soknadId = soknadId, listedHandlers = listedHandlers)
    }

    private fun doRunListedHandlers(
        soknadId: UUID,
        listedHandlers: List<RegisterDataHandler>,
    ) {
        listedHandlers.forEach {
            runCatching { it.handle(soknadId) }
                .onFailure { logger.error("Feil i innhenting av Register-data", it) }
        }
    }
}
