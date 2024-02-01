package no.nav.sosialhjelp.soknad.v2.adresse

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Unprotected
//@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknad/{soknadId}/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseController(
    private val adresseService: AdresseService
) {
    @GetMapping
    fun getAdresser(
        @PathVariable("soknadId") soknadId: UUID
    ): AdresserDto {
        return adresseService.getAdresserSoknad(soknadId).toAdresserDto()
    }

    @PutMapping
    fun updateAdresser(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) adresserInput: AdresserInput
    ): AdresserDto {
        // TODO Validere format på adresse?
        // TODO Validere at valgt adresse ikke er null?

        val adresser = adresseService.updateAdresseBruker(
            soknadId = soknadId,
            brukerInputAdresse = BrukerInputAdresse(
                valgtAdresse = adresserInput.valgtAdresse,
                brukerAdresse = adresserInput.adresseBruker
            )
        )

        return adresser.toAdresserDto()
    }
}

data class AdresserInput(
    val valgtAdresse: AdresseValg,
    val adresseBruker: Adresse?
)

data class AdresserDto(
    val valgtAdresse: AdresseValg? = null,
    val folkeregistrertAdresse: Adresse? = null,
    val midlertidigAdresse: Adresse? = null,
    val adresseBruker: Adresse? = null,
)

fun AdresserSoknad.toAdresserDto() =
    AdresserDto(
        valgtAdresse = brukerInput?.valgtAdresse,
        adresseBruker = brukerInput?.brukerAdresse,
        midlertidigAdresse = midlertidigAdresse,
        folkeregistrertAdresse = folkeregistrertAdresse,
    )
