package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class HandleUtbetalingerNav : RegisterDataHandler {
    override fun handle(soknadId: UUID) {
        TODO("Not yet implemented")
    }
}
