package no.nav.sosialhjelp.soknad.app.health

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/internal")
class InternalRessurs {
    @GetMapping("/isAlive", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun isAlive(): String {
        return "{status : \"ok\", message: \"Appen fungerer\"}"
    }
}
