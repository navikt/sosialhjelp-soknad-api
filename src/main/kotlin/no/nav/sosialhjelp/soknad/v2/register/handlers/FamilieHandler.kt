package no.nav.sosialhjelp.soknad.v2.register.handlers

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import org.springframework.stereotype.Component

@Component
class FamilieHandler: RegisterDataHandler {
    override fun fetchAndSave(soknadId: UUID) {
        TODO("Not yet implemented")
    }
}