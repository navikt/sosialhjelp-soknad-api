package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/inntekt/bostotte", produces = [MediaType.APPLICATION_JSON_VALUE])
class BostotteController(
    private val bostotteService: BostotteService,
) {
    @GetMapping
    fun getBostotte(
        @PathVariable("soknadId") soknadId: UUID,
    ): BostotteDto {
        return bostotteService.getBostotteInfo(soknadId).toBostotteDto()
    }

    @PutMapping
    fun updateBostotte(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: BostotteInput,
    ): BostotteDto {
        bostotteService.updateBostotte(soknadId, input.hasBostotte)
        return getBostotte(soknadId)
    }

    @PostMapping
    fun updateSamtykke(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody hasSamtykke: Boolean,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): BostotteDto {
        bostotteService.updateSamtykke(soknadId, hasSamtykke, token)
        return getBostotte(soknadId)
    }
}

private fun BostotteInfo.toBostotteDto() =
    BostotteDto(
        hasBostotte = bostotte?.verdi,
        hasSamtykke = samtykke?.verdi,
        samtykkeTidspunkt = samtykke?.dato,
        utbetalinger = utbetalinger.flatMap { inntekt -> inntekt.inntektDetaljer.detaljer.map { it.toUtbetalingBostotteDto() } },
        saker = saker.map { it.toBostotteSakDto() },
        fetchHusbankenFeilet = fetchHusbankenFeilet,
    )

private fun OkonomiDetalj.toUtbetalingBostotteDto(): UtbetalingBostotteDto {
    return when (this) {
        is Utbetaling ->
            UtbetalingBostotteDto(
                mottaker = mottaker,
                netto = netto,
                utbetalingsdato = utbetalingsdato,
            )
        else -> error("Feil type OkonomiDetalj lagret for UTBETALING_HUSBANKEN")
    }
}

data class BostotteDto(
    val hasBostotte: Boolean?,
    val hasSamtykke: Boolean?,
    val samtykkeTidspunkt: LocalDate?,
    val utbetalinger: List<UtbetalingBostotteDto>,
    val saker: List<BostotteSakDto>,
    val fetchHusbankenFeilet: Boolean?,
)

// TODO Hvilke felt er egentlig interessante for frontend?
data class UtbetalingBostotteDto(
    val mottaker: Mottaker?,
    val netto: Double?,
    val utbetalingsdato: LocalDate?,
) {
    val type = InntektType.UTBETALING_HUSBANKEN
}

data class BostotteSakDto(
    val dato: LocalDate,
    val status: BostotteStatus,
    val beskrivelse: String?,
    val vedtaksstatus: Vedtaksstatus?,
)

private fun BostotteSak.toBostotteSakDto() =
    BostotteSakDto(
        dato = dato,
        status = status,
        beskrivelse = beskrivelse,
        vedtaksstatus = vedtaksstatus,
    )

data class BostotteInput(val hasBostotte: Boolean)

data class SamtykkeInput(val hasSamtykke: Boolean)
