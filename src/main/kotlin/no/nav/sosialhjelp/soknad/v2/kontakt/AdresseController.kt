package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseController(
    private val adresseService: AdresseService,
) {
    @GetMapping
    fun getAdresser(
        @PathVariable("soknadId") soknadId: UUID,
    ): AdresserDto {
        return createAdresseDto(
            adresser = adresseService.findAdresser(soknadId),
            mottaker = adresseService.findMottaker(soknadId),
        )
    }

    @PutMapping
    fun updateAdresser(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) adresserInput: AdresserInput,
    ): AdresserDto {
        return adresseService
            .updateBrukerAdresse(
                soknadId = soknadId,
                adresseValg = adresserInput.adresseValg,
                brukerAdresse = adresserInput.brukerAdresse,
            )
            .let {
                createAdresseDto(
                    adresser = it,
                    mottaker = adresseService.findMottaker(soknadId),
                )
            }
    }
}

data class AdresserInput(
    val adresseValg: AdresseValg,
    val brukerAdresse: Adresse?,
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

fun createAdresseDto(
    adresser: Adresser,
    mottaker: NavEnhet?,
): AdresserDto {
    return AdresserDto(
        adresseValg = adresser.adressevalg,
        brukerAdresse = adresser.fraBruker,
        midlertidigAdresse = adresser.midlertidig,
        folkeregistrertAdresse = adresser.folkeregistrert,
        navenhet = mottaker?.toNavEnhetDto(),
    )
}

fun NavEnhet.toNavEnhetDto(): NavEnhetDto {
    return NavEnhetDto(
        enhetsnavn = enhetsnavn,
        orgnummer = orgnummer,
        enhetsnummer = enhetsnummer,
        kommunenummer = kommunenummer,
    )
}
