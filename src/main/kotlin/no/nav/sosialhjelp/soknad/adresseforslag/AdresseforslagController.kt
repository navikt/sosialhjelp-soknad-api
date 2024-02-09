package no.nav.sosialhjelp.soknad.adresseforslag

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseCompletionResult
import no.nav.sosialhjelp.soknad.adressesok.AdresseforslagService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/adresseforslag")
@Unprotected
class AdresseforslagController(private val adresseforslagService: AdresseforslagService) {
    @GetMapping()
    fun adresseSok(
        @RequestParam("sokestreng") sokestreng: String?
    ): AdresseCompletionResult? {
        requireNotNull(sokestreng) { "sokestreng is required" }

        return adresseforslagService.find(sokestreng)
    }
}
