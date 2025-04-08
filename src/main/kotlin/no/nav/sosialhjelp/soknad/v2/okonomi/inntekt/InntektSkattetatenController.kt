package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
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
@RequestMapping("/soknad/{soknadId}/inntekt/skattbarinntekt", produces = [MediaType.APPLICATION_JSON_VALUE])
class InntektSkattetatenController(
    private val useCaseHandler: InntektSkatteetatenUseCaseHandler,
) {
    @GetMapping
    fun getSkattbarInntekt(
        @PathVariable("soknadId") soknadId: UUID,
    ): SkattbarInntektDto {
        return useCaseHandler.getInntektSkattInfo(soknadId)
            .let {
                SkattbarInntektDto(
                    inntektSkatteetaten = it.inntekt?.toInntektFraOrganisasjonDtos() ?: emptyList(),
                    inntektFraSkatteetatenFeilet = it.getFailed,
                    samtykke = it.samtykke?.toSamtykkeDto(),
                )
            }
    }

    @PutMapping("/samtykke")
    fun updateSamtykke(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody samtykke: Boolean,
    ): SkattbarInntektDto {
        useCaseHandler.updateSamtykke(soknadId, samtykke)
        return getSkattbarInntekt(soknadId)
    }
}

private fun Inntekt.toInntektFraOrganisasjonDtos(): List<InntektFraOrganisasjonDto> {
    return inntektDetaljer.detaljer
        .map {
            if (it is Utbetaling) {
                it
            } else {
                throw IllegalStateException("Feil Detaljtype for inntektDetaljer: ${it::class}")
            }
        }
        .sortedByDescending { it.periodeFom }
        .groupBy { it.periodeFom }
        .values
        .map { it.toInntektFraOrganisasjonDto() }
}

private fun List<Utbetaling>.toInntektFraOrganisasjonDto(): InntektFraOrganisasjonDto {
    return this
        .groupBy { it.organisasjon }
        .map { (org, utbetalinger) ->
            OrganisasjonDto(
                organisasjonsnavn = org?.navn,
                orgnr = org?.orgnummer,
                fom = utbetalinger.first().periodeFom?.toString(),
                tom = utbetalinger.first().periodeTom?.toString(),
                utbetalinger = utbetalinger.map { it.toUtbetalingDto() },
            )
        }
        .let { dtos -> InntektFraOrganisasjonDto(dtos) }
}

private fun Utbetaling.toUtbetalingDto(): UtbetalingFraSkatteetatenDto {
    return UtbetalingFraSkatteetatenDto(
        brutto = brutto,
        forskuddstrekk = skattetrekk,
        tittel = tittel,
    )
}

private fun Bekreftelse.toSamtykkeDto() = SamtykkeDto(verdi, samtykkeTidspunkt = tidspunkt.toString())

data class SkattbarInntektDto(
    val inntektSkatteetaten: List<InntektFraOrganisasjonDto>,
    val inntektFraSkatteetatenFeilet: Boolean? = null,
    val samtykke: SamtykkeDto? = null,
)

data class InntektFraOrganisasjonDto(
    val organisasjoner: List<OrganisasjonDto>,
)

data class SamtykkeDto(
    val verdi: Boolean,
    val samtykkeTidspunkt: String,
)

data class OrganisasjonDto(
    val utbetalinger: List<UtbetalingFraSkatteetatenDto>,
    val organisasjonsnavn: String?,
    val orgnr: String?,
    val fom: String?,
    val tom: String?,
)

data class UtbetalingFraSkatteetatenDto(
    val brutto: Double?,
    val forskuddstrekk: Double?,
    val tittel: String?,
)
