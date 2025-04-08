package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/inntekt/bostotte", produces = [MediaType.APPLICATION_JSON_VALUE])
class BostotteController(
    private val bostotteUseCaseHandler: BostotteUseCaseHandler,
) {
    @GetMapping
    fun getBostotte(
        @PathVariable("soknadId") soknadId: UUID,
    ): BostotteDto {
        return bostotteUseCaseHandler.getBostotteInfo(soknadId).toBostotteDto()
    }

    @PostMapping
    fun updateBostotte(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: BostotteInput,
    ): BostotteDto {
        bostotteUseCaseHandler.updateBostotte(soknadId, input.hasBostotte, input.hasSamtykke)

        return getBostotte(soknadId)
    }
}

private fun BostotteInfo.toBostotteDto() =
    BostotteDto(
        hasBostotte = bostotte?.verdi,
        hasSamtykke = samtykke?.verdi,
        samtykkeTidspunkt = samtykke?.tidspunkt,
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
        // Belop betyr at bruker har tastet inn dette som en okonomisk opplysning -> dette omhandler data fra register
        is Belop -> UtbetalingBostotteDto(null, null, null)
        else -> error("Feil type OkonomiDetalj lagret for UTBETALING_HUSBANKEN: ${this.javaClass}")
    }
}

data class BostotteDto(
    val hasBostotte: Boolean?,
    val hasSamtykke: Boolean?,
    val samtykkeTidspunkt: LocalDateTime?,
    val utbetalinger: List<UtbetalingBostotteDto>,
    val saker: List<BostotteSakDto>,
    val fetchHusbankenFeilet: Boolean?,
)

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

data class BostotteInput(val hasBostotte: Boolean? = null, val hasSamtykke: Boolean? = null)
