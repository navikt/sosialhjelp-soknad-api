package no.nav.sosialhjelp.soknad.adresseforslag

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.pdl.types.AdresseCompletionResult
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/adresseforslag", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseforslagController(private val adresseforslagService: AdresseforslagService) {
    @GetMapping()
    fun adresseForslag(
        @RequestParam("sokestreng") sokestreng: String
    ): AdresseCompletionResult? = adresseforslagService.find(sokestreng)
}
