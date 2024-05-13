package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtbetalingNavHandler : RegisterDataFetcher {
    override fun fetchAndSave(soknadId: UUID) {
        TODO("Not yet implemented")
    }
}
