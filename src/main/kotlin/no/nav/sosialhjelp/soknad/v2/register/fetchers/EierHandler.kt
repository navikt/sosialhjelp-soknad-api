package no.nav.sosialhjelp.soknad.v2.register.fetchers

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import org.springframework.stereotype.Component

@Component
class EierHandler(
    private val eierRepository: EierRepository
): RegisterDataHandler {
    override fun fetchAndSave(soknadId: UUID) {
        TODO("Not yet implemented")
    }
}