package no.nav.sosialhjelp.soknad.app.health

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.selftest.SelftestResult
import no.nav.sosialhjelp.selftest.SelftestService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/internal")
open class InternalRessurs(
    private val selftestService: SelftestService
) {

    @GetMapping("/isAlive", produces = [MediaType.TEXT_PLAIN_VALUE])
    open fun isAlive(): String {
        return "{status : \"ok\", message: \"Appen fungerer\"}"
    }

    @GetMapping("/selftest", produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getSelftest(): SelftestResult {
        return selftestService.getSelftest()
    }
}
