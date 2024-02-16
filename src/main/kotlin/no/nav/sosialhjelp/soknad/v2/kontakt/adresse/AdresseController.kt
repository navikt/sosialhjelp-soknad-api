package no.nav.sosialhjelp.soknad.v2.kontakt.adresse

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktService
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
    private val kontaktService: KontaktService,
    private val soknadService: SoknadService
) {
    @GetMapping
    fun getAdresser(
        @PathVariable("soknadId") soknadId: UUID
    ): AdresserDto {
        val navEnhet = soknadService.getSoknad(soknadId).mottaker
        return kontaktService.getAdresser(soknadId).toAdresserDto(navEnhet)
    }

    @PutMapping
    fun updateAdresser(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) adresserInput: AdresserInput
    ): AdresserDto {
        // TODO Validere format på adresse?

        val adresser = kontaktService.updateBrukerAdresse(
            soknadId = soknadId,
            adresseValg = adresserInput.adresseValg,
            brukerAdresse = adresserInput.brukerAdresse
        )
        val navEnhet = soknadService.getSoknad(soknadId).mottaker
        return adresser.toAdresserDto(navEnhet)
    }
}

data class AdresserInput(
    val adresseValg: AdresseValg,
    val brukerAdresse: Adresse?
)

data class AdresserDto(
    val adresseValg: AdresseValg? = null,
    val folkeregistrertAdresse: Adresse? = null,
    val midlertidigAdresse: Adresse? = null,
    val brukerAdresse: Adresse? = null,
    val navenhet: NavEnhetDto? = null,
)

data class NavEnhetDto(
    val enhetsnavn: String? = null,
    val orgnummer: String? = null,
    val enhetsnummer: String? = null,
    val kommunenummer: String? = null,
    val kommunenavn: String? = null,
)

fun Adresser.toAdresserDto(navenhet: NavEnhet?) =
    AdresserDto(
        adresseValg = adressevalg,
        brukerAdresse = brukerAdresse,
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
