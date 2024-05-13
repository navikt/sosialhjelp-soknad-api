package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtbetalingNavHandler : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        // TODO Not implemented
        logger.info("Henter utbetalinger fra NAV. (Ikke implementert)")
    }
}
