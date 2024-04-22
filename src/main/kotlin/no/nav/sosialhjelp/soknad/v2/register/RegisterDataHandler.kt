package no.nav.sosialhjelp.soknad.v2.register

import java.util.UUID
import org.springframework.stereotype.Service

interface RegisterDataHandler {
    fun fetchAndSave(soknadId: UUID)
}

@Service
class RegisterDataService(
    private val handlers: List<RegisterDataHandler>
) {

    fun fetchAndSave(soknadId: UUID) {

        handlers.forEach { it.fetchAndSave(soknadId) }

    }
}
