package no.nav.sosialhjelp.soknad.v2.kontakt

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
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
    private val adresseUseCaseHandler: AdresseUseCaseHandler,
) {
    @GetMapping
    fun getAdresser(
        @PathVariable("soknadId") soknadId: UUID,
    ): AdresserDto = adresseUseCaseHandler.getAdresseAndMottakerInfo(soknadId)

    @PutMapping
    fun updateAdresser(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) adresserInput: AdresserInput,
    ): AdresserDto {
        adresseUseCaseHandler.updateBrukerAdresse(
            soknadId = soknadId,
            adresseValg = adresserInput.adresseValg,
            brukerAdresse = adresserInput.brukerAdresse?.toDomainAdresse(),
        )

        return getAdresser(soknadId)
    }
}

private fun AdresseInput.toDomainAdresse(): Adresse? =
    this.takeIf { it is VegAdresse || it is MatrikkelAdresse } as? Adresse

data class AdresserInput(
    val adresseValg: AdresseValg,
    val brukerAdresse: AdresseInput?,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = VegAdresse::class, name = "VegAdresse"),
    JsonSubTypes.Type(value = MatrikkelAdresse::class, name = "MatrikkelAdresse"),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "VegAdresse", schema = VegAdresse::class),
        DiscriminatorMapping(value = "MatrikkelAdresse", schema = MatrikkelAdresse::class),
    ],
    subTypes = [VegAdresse::class, MatrikkelAdresse::class],
)
interface AdresseInput

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
    val isMottakMidlertidigDeaktivert: Boolean? = null,
    val isMottakDeaktivert: Boolean? = null,
)

fun createAdresseDto(
    adresser: Adresser,
    mottaker: NavEnhetDto?,
): AdresserDto =
    AdresserDto(
        adresseValg = adresser.adressevalg,
        brukerAdresse = adresser.fraBruker,
        midlertidigAdresse = adresser.midlertidig,
        folkeregistrertAdresse = adresser.folkeregistrert,
        navenhet = mottaker,
    )

fun NavEnhet.toNavEnhetDto(enrichment: KommuneInfo?): NavEnhetDto =
    NavEnhetDto(
        enhetsnavn = enhetsnavn,
        orgnummer = orgnummer,
        enhetsnummer = enhetsnummer,
        kommunenummer = kommunenummer,
        kommunenavn = kommunenavn,
        isMottakDeaktivert = enrichment?.isDigisosKommune == false,
        isMottakMidlertidigDeaktivert = enrichment?.isDigisosKommune == false,
    )
