package no.nav.sosialhjelp.soknad.v2.adresse

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.soknad.NavEnhet
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
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
// @ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknad/{soknadId}/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseController(
    private val adresseService: AdresseService,
    private val soknadService: SoknadService
) {
    @GetMapping
    fun getAdresser(
        @PathVariable("soknadId") soknadId: UUID
    ): AdresserDto {
        val navEnhet = soknadService.getSoknad(soknadId).navEnhet
        return adresseService.getAdresserSoknad(soknadId).toAdresserDto(navEnhet)
    }

    @PutMapping
    fun updateAdresser(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) adresserInput: AdresserInput
    ): AdresserDto {
        // TODO Validere format på adresse?

        val adresser = adresseService.updateAdresseBruker(
            soknadId = soknadId,
            brukerInputAdresse = BrukerInputAdresse(
                valgtAdresse = adresserInput.valgtAdresse,
                brukerAdresse = adresserInput.adresseBruker
            )
        )
        val navEnhet = soknadService.getSoknad(soknadId).navEnhet
        return adresser.toAdresserDto(navEnhet)
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
    val navenhet: NavEnhetDto? = null,
)

data class NavEnhetDto(
    val enhetsnavn: String? = null,
    val orgnummer: String? = null,
    val enhetsnummer: String? = null,
    val kommunenummer: String? = null,
    val kommunenavn: String? = null,
)

fun AdresserSoknad.toAdresserDto(navenhet: NavEnhet?) =
    AdresserDto(
        valgtAdresse = brukerInput?.valgtAdresse,
        adresseBruker = brukerInput?.brukerAdresse,
        midlertidigAdresse = midlertidigAdresse,
        folkeregistrertAdresse = folkeregistrertAdresse,
        navenhet = navenhet?.toNavEnhetDto()
    )

fun NavEnhet.toNavEnhetDto(): NavEnhetDto {
    return NavEnhetDto(
        enhetsnavn = enhetsnavn,
        orgnummer = orgnummer,
        enhetsnummer = enhetsnummer,
        kommunenummer = kommunenummer,
    )
}
