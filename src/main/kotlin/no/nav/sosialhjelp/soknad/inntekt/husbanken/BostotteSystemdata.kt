package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Bostotte
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Sak
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Utbetaling
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.apache.commons.text.WordUtils
import org.springframework.stereotype.Component
import java.time.LocalDate
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService.Companion.nowWithForcedMicros

@Component
class BostotteSystemdata(
    private val husbankenClient: HusbankenClient,
    private val textService: TextService,
) {
    fun updateSystemdataIn(
        soknadUnderArbeid: SoknadUnderArbeid,
        token: String?,
    ) {
        val soknad = soknadUnderArbeid.jsonInternalSoknad?.soknad ?: return
        val okonomi = soknad.data.okonomi
        if (okonomi.opplysninger.bekreftelse.any { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi }) {
            val bostotte = innhentBostotteFraHusbanken(token)
            if (bostotte != null) {
                okonomi.opplysninger.bekreftelse
                    .firstOrNull { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) }
                    ?.withBekreftelsesDato(nowWithForcedMicros())
                fjernGamleHusbankenData(okonomi, false)
                val trengerViDataFraDeSiste60Dager = !harViDataFraSiste30Dager(bostotte)
                val jsonBostotteUtbetalinger =
                    mapToJsonOkonomiOpplysningUtbetalinger(bostotte, trengerViDataFraDeSiste60Dager)
                okonomi.opplysninger.utbetaling.addAll(jsonBostotteUtbetalinger)
                val jsonSaksStatuser = mapToBostotteSaker(bostotte, trengerViDataFraDeSiste60Dager)
                okonomi.opplysninger.bostotte.saker.addAll(jsonSaksStatuser)
                soknad.driftsinformasjon.stotteFraHusbankenFeilet = false
            } else {
                soknad.driftsinformasjon.stotteFraHusbankenFeilet = true
            }
        } else { // Ikke samtykke!
            fjernGamleHusbankenData(okonomi, true)
            soknad.driftsinformasjon.stotteFraHusbankenFeilet = false
        }
    }

    private fun fjernGamleHusbankenData(
        okonomi: JsonOkonomi,
        skalFortsattHaBrukerUtbetaling: Boolean,
    ) {
        val okonomiopplysninger = okonomi.opplysninger
        okonomiopplysninger.bostotte = JsonBostotte()

        okonomiopplysninger.utbetaling.removeIf {
            it.type.equals(UTBETALING_HUSBANKEN, ignoreCase = true) && it.kilde == JsonKilde.SYSTEM
        }
        if (skalFortsattHaBrukerUtbetaling) {
            val tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[SoknadJsonTyper.BOSTOTTE])
            addUtbetalingIfNotPresentInOpplysninger(okonomiopplysninger.utbetaling, UTBETALING_HUSBANKEN, tittel)
        } else {
            removeUtbetalingIfPresentInOpplysninger(okonomiopplysninger.utbetaling, UTBETALING_HUSBANKEN)
        }
    }

    private fun harViDataFraSiste30Dager(bostotte: Bostotte): Boolean {
        val harNyeSaker = bostotte.saker.any { it.dato.isAfter(LocalDate.now().minusDays(30)) }
        val harNyeUtbetalinger = bostotte.utbetalinger.any { it.utbetalingsdato.isAfter(LocalDate.now().minusDays(30)) }
        return harNyeSaker || harNyeUtbetalinger
    }

    private fun innhentBostotteFraHusbanken(token: String?): Bostotte? {
        val bostotteDto = husbankenClient.hentBostotte(token, LocalDate.now().minusDays(60), LocalDate.now())
        if (bostotteDto?.saker.isNullOrEmpty()) {
            log.info("BostotteDto.saker er null eller tom")
        }
        if (bostotteDto?.utbetalinger.isNullOrEmpty()) {
            log.info("BostotteDto.utbetalinger er null eller tom")
        }
        return bostotteDto?.toDomain()
    }

    private fun mapToJsonOkonomiOpplysningUtbetalinger(
        bostotte: Bostotte,
        trengerViDataFraDeSiste60Dager: Boolean,
    ): List<JsonOkonomiOpplysningUtbetaling> {
        val filterDays = if (trengerViDataFraDeSiste60Dager) 60 else 30
        return bostotte.utbetalinger
            .filter { it.utbetalingsdato.isAfter(LocalDate.now().minusDays(filterDays.toLong())) }
            .map { mapToJsonOkonomiOpplysningUtbetaling(it) }
    }

    private fun mapToJsonOkonomiOpplysningUtbetaling(utbetaling: Utbetaling): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.SYSTEM)
            .withType(UTBETALING_HUSBANKEN)
            .withTittel("Statlig bostøtte")
            .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.fromValue(gjorForsteBokstavStor(utbetaling.mottaker.toString())))
            .withNetto(utbetaling.belop.toDouble())
            .withUtbetalingsdato(utbetaling.utbetalingsdato.toString())
            .withOverstyrtAvBruker(false)
    }

    private fun gjorForsteBokstavStor(navn: String): String {
        return WordUtils.capitalizeFully(navn)
    }

    private fun mapToBostotteSaker(
        bostotte: Bostotte,
        trengerViDataFraDeSiste60Dager: Boolean,
    ): List<JsonBostotteSak> {
        val filterDays = if (trengerViDataFraDeSiste60Dager) 60 else 30
        return bostotte.saker
            .filter { it.dato.isAfter(LocalDate.now().minusDays(filterDays.toLong())) }
            .map { mapToBostotteSak(it) }
    }

    private fun mapToBostotteSak(sak: Sak): JsonBostotteSak {
        val bostotteSak =
            JsonBostotteSak()
                .withKilde(JsonKildeSystem.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withStatus(sak.status.toString())
                .withDato(sak.dato.toString())
        if (sak.vedtak != null) {
            bostotteSak.withBeskrivelse(sak.vedtak.beskrivelse)
            bostotteSak.withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.fromValue(sak.vedtak.type))
        }
        return bostotteSak
    }

    companion object {
        private val log by logger()
    }
}
