package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.booleanVerdiFelt
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.getBekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue

class BostotteHusbanken {

    fun getAvsnitt(opplysninger: JsonOkonomiopplysninger, driftsinformasjon: JsonDriftsinformasjon): Avsnitt {
        return Avsnitt(
            tittel = "inntekt.bostotte.husbanken.tittel",
            sporsmal = bostotteSporsmal(opplysninger, driftsinformasjon)
        )
    }

    private fun bostotteSporsmal(
        opplysninger: JsonOkonomiopplysninger,
        driftsinformasjon: JsonDriftsinformasjon
    ): List<Sporsmal> {
        val harUtfyltBostotteSporsmal = harBekreftelse(opplysninger, SoknadJsonTyper.BOSTOTTE)
        val harSvartJaBostotte = harUtfyltBostotteSporsmal && harBekreftelseTrue(opplysninger, SoknadJsonTyper.BOSTOTTE)
        val harBostotteSamtykke = harSvartJaBostotte && harBekreftelseTrue(opplysninger, BOSTOTTE_SAMTYKKE)
        val fikkFeilMotHusbanken = java.lang.Boolean.TRUE == driftsinformasjon.stotteFraHusbankenFeilet
        val sporsmal = mutableListOf<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = "inntekt.bostotte.sporsmal.sporsmal",
                erUtfylt = harUtfyltBostotteSporsmal,
                felt = if (harUtfyltBostotteSporsmal) booleanVerdiFelt(
                    harSvartJaBostotte,
                    "inntekt.bostotte.sporsmal.true",
                    "inntekt.bostotte.sporsmal.false"
                ) else null
            )
        )
        if (harSvartJaBostotte && fikkFeilMotHusbanken) {
            sporsmal.add(
                Sporsmal(
                    tittel = "inntekt.bostotte.kontaktproblemer",
                    erUtfylt = true,
                    felt = null
                )
            )
        }
        if (harSvartJaBostotte && !fikkFeilMotHusbanken && !harBostotteSamtykke) {
            sporsmal.add(
                Sporsmal(
                    tittel = "inntekt.bostotte.mangler_samtykke",
                    erUtfylt = true,
                    felt = null
                )
            )
        }
        if (harSvartJaBostotte && !fikkFeilMotHusbanken && harBostotteSamtykke) {
            val harUtbetalinger = harHusbankenUtbetalinger(opplysninger)
            val harSaker = opplysninger.bostotte.saker.isNotEmpty()
            getBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE)
                ?.let { bekreftelseTidspunktSporsmal(it) }
                ?.let { sporsmal.add(it) }
            if (!harUtbetalinger && !harSaker) {
                sporsmal.add(sporsmalMedIngenUtbetalingerEllerSakerSvar())
            } else {
                sporsmal.add(utbetalingerSporsmal(opplysninger))
                sporsmal.add(sakerSporsmal(opplysninger))
            }
        }
        return sporsmal
    }

    private fun bekreftelseTidspunktSporsmal(bostotteBekreftelse: JsonOkonomibekreftelse): Sporsmal {
        return Sporsmal(
            tittel = "inntekt.bostotte.har_gitt_samtykke",
            erUtfylt = true,
            felt = listOf(
                Felt(
                    type = Type.TEKST,
                    svar = createSvar(bostotteBekreftelse.bekreftelsesDato, SvarType.TIDSPUNKT)
                )
            )
        )
    }

    private fun sporsmalMedIngenUtbetalingerEllerSakerSvar(): Sporsmal {
        return Sporsmal(
            tittel = "",
            erUtfylt = true,
            felt = listOf(
                Felt(
                    type = Type.TEKST,
                    svar = createSvar("inntekt.bostotte.ikkefunnet", SvarType.LOCALE_TEKST)
                )
            )
        )
    }

    private fun utbetalingerSporsmal(opplysninger: JsonOkonomiopplysninger): Sporsmal {
        val harUtbetalinger = harHusbankenUtbetalinger(opplysninger)
        val harSaker = opplysninger.bostotte.saker.isNotEmpty()
        val felter: List<Felt>
        if (!harUtbetalinger && harSaker) {
            felter = listOf(
                Felt(
                    type = Type.TEKST,
                    svar = createSvar("inntekt.bostotte.utbetalingerIkkefunnet", SvarType.LOCALE_TEKST)
                )
            )
        } else {
            felter = opplysninger.utbetaling
                .filter { UTBETALING_HUSBANKEN == it.type }
                .map {
                    val map = LinkedHashMap<String, Svar>()
                    if (it.mottaker == null) {
                        log.warn("Utbetaling.mottaker er null?")
                    }
                    map["inntekt.bostotte.utbetaling.mottaker"] = createSvar(if (it.mottaker == null) "" else it.mottaker.value(), SvarType.TEKST)
                    map["inntekt.bostotte.utbetaling.utbetalingsdato"] = createSvar(it.utbetalingsdato, SvarType.DATO)
                    map["inntekt.bostotte.utbetaling.belop"] = createSvar(it.netto.toString(), SvarType.TEKST)
                    Felt(
                        type = Type.SYSTEMDATA_MAP,
                        labelSvarMap = map
                    )
                }
        }
        return Sporsmal(
            tittel = "inntekt.bostotte.utbetaling",
            erUtfylt = true,
            felt = felter
        )
    }

    private fun sakerSporsmal(opplysninger: JsonOkonomiopplysninger): Sporsmal {
        val harUtbetalinger = harHusbankenUtbetalinger(opplysninger)
        val harSaker = opplysninger.bostotte.saker.isNotEmpty()
        val felter: List<Felt>
        if (harUtbetalinger && !harSaker) {
            felter = listOf(
                Felt(
                    type = Type.TEKST,
                    svar = createSvar("inntekt.bostotte.sakerIkkefunnet", SvarType.LOCALE_TEKST)
                )
            )
        } else {
            felter = opplysninger.bostotte.saker
                .map {
                    val map = LinkedHashMap<String, Svar>()
                    map["inntekt.bostotte.sak.dato"] = createSvar(it.dato, SvarType.DATO)
                    map["inntekt.bostotte.sak.status"] = createSvar(bostotteSakStatus(it), SvarType.TEKST)
                    Felt(
                        type = Type.SYSTEMDATA_MAP,
                        labelSvarMap = map
                    )
                }
        }
        return Sporsmal(
            tittel = "inntekt.bostotte.sak",
            erUtfylt = true,
            felt = felter
        )
    }

    private fun harHusbankenUtbetalinger(opplysninger: JsonOkonomiopplysninger): Boolean {
        return opplysninger.utbetaling.any { UTBETALING_HUSBANKEN == it.type }
    }

    private fun bostotteSakStatus(sak: JsonBostotteSak): String {
        var status = if (sak.vedtaksstatus != null) sak.vedtaksstatus.value() else sak.status
        if (sak.beskrivelse != null && sak.beskrivelse.isNotBlank()) {
            status += ": ${sak.beskrivelse}"
        }
        return status
    }

    companion object {
        private val log by logger()
    }
}
