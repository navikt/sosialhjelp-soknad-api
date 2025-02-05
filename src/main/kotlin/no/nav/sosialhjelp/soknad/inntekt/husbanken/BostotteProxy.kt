package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteController
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteDto
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteInput
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteSakDto
import no.nav.sosialhjelp.soknad.v2.bostotte.SamtykkeInput
import no.nav.sosialhjelp.soknad.v2.bostotte.UtbetalingBostotteDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BostotteProxy(private val bostotteController: BostotteController) {
    fun getBostotte(behandlingsId: String): BostotteRessurs.BostotteFrontend {
        return bostotteController
            .getBostotte(UUID.fromString(behandlingsId))
            .toBostotteFrontend()
    }

    fun updateBostotteBekreftelse(
        soknadId: String,
        hasBostotte: Boolean?,
    ) {
        hasBostotte?.also { doUpdateHasBostotte(soknadId, it) }
    }

    private fun doUpdateHasBostotte(
        soknadId: String,
        hasBostotte: Boolean,
        retry: Int = 1,
    ) {
        while (retry <= 5) {
            try {
                if (retry != 1) logger.warn("Retry $retry for updateHasBostotte")
                bostotteController.updateHasBostotte(UUID.fromString(soknadId), BostotteInput(hasBostotte))
            } catch (e: Exception) {
                when (retry < 5) {
                    true -> doUpdateHasBostotte(soknadId, hasBostotte, retry + 1)
                    false -> throw RuntimeException("Kunne ikke oppdatere bostøtte", e)
                }
            }
        }
    }

    fun updateBostotteSamtykke(
        behandlingsId: String,
        samtykke: Boolean,
        token: String?,
    ) {
        doUpdateSamtykke(behandlingsId, samtykke, token)
    }

    private fun doUpdateSamtykke(
        soknadId: String,
        samtykke: Boolean,
        token: String?,
        retry: Int = 1,
    ) {
        while (retry <= 5) {
            try {
                if (retry != 1) logger.warn("Retry $retry for update Samtykke")
                bostotteController.updateHasSamtykke(UUID.fromString(soknadId), SamtykkeInput(samtykke), token)
            } catch (e: Exception) {
                when (retry < 5) {
                    true -> doUpdateSamtykke(soknadId, samtykke, token, retry + 1)
                    false -> throw RuntimeException("Kunne ikke oppdatere Samtykke for bostotte", e)
                }
            }
        }
    }

    companion object {
        private val logger by logger()
    }
}

private fun UtbetalingBostotteDto.toJsonOkonomiOpplysningUtbetaling() =
    JsonOkonomiOpplysningUtbetaling()
        .withKilde(JsonKilde.SYSTEM)
        .withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)
        .withTittel("Statlig bostøtte")
        .withMottaker(mottaker?.let { JsonOkonomiOpplysningUtbetaling.Mottaker.valueOf(it.name) })
        .withNetto(netto)
        .withUtbetalingsdato(utbetalingsdato?.toString())
        .withOverstyrtAvBruker(false)

private fun BostotteDto.toBostotteFrontend(): BostotteRessurs.BostotteFrontend {
    return BostotteRessurs.BostotteFrontend(
        bekreftelse = hasBostotte,
        samtykke = hasSamtykke,
        samtykkeTidspunkt = samtykkeTidspunkt?.toString(),
        stotteFraHusbankenFeilet = fetchHusbankenFeilet,
        utbetalinger = utbetalinger.map { it.toJsonOkonomiOpplysningUtbetaling() },
        saker = saker.map { it.toJsonOkonomiOpplysningSak() },
    )
}

private fun BostotteSakDto.toJsonOkonomiOpplysningSak() =
    JsonBostotteSak()
        .withKilde(JsonKildeSystem.SYSTEM)
        .withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)
        .withStatus(status.toString())
        .withDato(dato.toString())
