package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import org.slf4j.LoggerFactory

class BostotteHusbanken {
    fun getAvsnitt(
        opplysninger: JsonOkonomiopplysninger,
        driftsinformasjon: JsonDriftsinformasjon,
    ): Avsnitt =
        Avsnitt(
            tittel = "inntekt.bostotte.husbanken.tittel",
            sporsmal = bostotteSporsmal(opplysninger, driftsinformasjon),
        )

    private fun bostotteSporsmal(
        opplysninger: JsonOkonomiopplysninger,
        driftsinformasjon: JsonDriftsinformasjon,
    ): List<Sporsmal> {
        val fikkFeilMotHusbanken = java.lang.Boolean.TRUE == driftsinformasjon.stotteFraHusbankenFeilet
        val sporsmal = mutableListOf<Sporsmal>()

        if (fikkFeilMotHusbanken) {
            sporsmal.add(
                Sporsmal(
                    tittel = "inntekt.bostotte.kontaktproblemer",
                    erUtfylt = true,
                    felt = null,
                ),
            )
        }
        if (!fikkFeilMotHusbanken) {
            val harUtbetalinger = harHusbankenUtbetalinger(opplysninger)
            val harSaker = opplysninger.bostotte?.saker?.isNotEmpty() == true
            if (!harUtbetalinger && !harSaker) {
                sporsmal.add(sporsmalMedIngenUtbetalingerEllerSakerSvar())
            } else {
                sporsmal.add(utbetalingerSporsmal(opplysninger))
                sporsmal.add(sakerSporsmal(opplysninger))
            }
        }
        return sporsmal
    }

    private fun sporsmalMedIngenUtbetalingerEllerSakerSvar(): Sporsmal =
        Sporsmal(
            tittel = "",
            erUtfylt = true,
            felt =
                listOf(
                    Felt(
                        type = Type.TEKST,
                        svar = createSvar("inntekt.bostotte.ikkefunnet", SvarType.LOCALE_TEKST),
                    ),
                ),
        )

    private fun utbetalingerSporsmal(opplysninger: JsonOkonomiopplysninger): Sporsmal {
        val harUtbetalinger = harHusbankenUtbetalinger(opplysninger)
        val harSaker = opplysninger.bostotte?.saker?.isNotEmpty() == true
        val felter: List<Felt>
        if (!harUtbetalinger && harSaker) {
            felter =
                listOf(
                    Felt(
                        type = Type.TEKST,
                        svar = createSvar("inntekt.bostotte.utbetalingerIkkefunnet", SvarType.LOCALE_TEKST),
                    ),
                )
        } else {
            felter =
                opplysninger.utbetaling
                    .filter { UTBETALING_HUSBANKEN == it.type }
                    .map {
                        val map = LinkedHashMap<String, Svar>()
                        if (it.mottaker == null) {
                            log.warn("Utbetaling.mottaker er null?")
                        }
                        map["inntekt.bostotte.utbetaling.mottaker"] =
                            createSvar(
                                it.mottaker?.value ?: "",
                                SvarType.TEKST,
                            )
                        map["inntekt.bostotte.utbetaling.utbetalingsdato"] = createSvar(it.utbetalingsdato, SvarType.DATO)
                        map["inntekt.bostotte.utbetaling.belop"] = createSvar(it.netto?.toString(), SvarType.TEKST)
                        Felt(
                            type = Type.SYSTEMDATA_MAP,
                            labelSvarMap = map,
                        )
                    }
        }
        return Sporsmal(
            tittel = "inntekt.bostotte.utbetaling",
            erUtfylt = true,
            felt = felter,
        )
    }

    private fun sakerSporsmal(opplysninger: JsonOkonomiopplysninger): Sporsmal {
        val harUtbetalinger = harHusbankenUtbetalinger(opplysninger)
        val harSaker = opplysninger.bostotte?.saker?.isNotEmpty() == true
        val felter: List<Felt>
        if (harUtbetalinger && !harSaker) {
            felter =
                listOf(
                    Felt(
                        type = Type.TEKST,
                        svar = createSvar("inntekt.bostotte.sakerIkkefunnet", SvarType.LOCALE_TEKST),
                    ),
                )
        } else {
            felter =
                opplysninger.bostotte?.saker.orEmpty()
                    .map {
                        val map = LinkedHashMap<String, Svar>()
                        map["inntekt.bostotte.sak.dato"] = createSvar(it.dato, SvarType.DATO)
                        map["inntekt.bostotte.sak.status"] = createSvar(bostotteSakStatus(it), SvarType.TEKST)
                        Felt(
                            type = Type.SYSTEMDATA_MAP,
                            labelSvarMap = map,
                        )
                    }
        }
        return Sporsmal(
            tittel = "inntekt.bostotte.sak",
            erUtfylt = true,
            felt = felter,
        )
    }

    private fun harHusbankenUtbetalinger(opplysninger: JsonOkonomiopplysninger): Boolean = opplysninger.utbetaling.any { UTBETALING_HUSBANKEN == it.type }

    private fun bostotteSakStatus(sak: JsonBostotteSak): String {
        var status = sak.vedtaksstatus?.value ?: sak.status
        val beskrivelse = sak.beskrivelse
        if (!beskrivelse.isNullOrBlank()) {
            status += ": $beskrivelse"
        }
        return status
    }

    companion object {
        private val log = LoggerFactory.getLogger(BostotteHusbanken::class.java)
    }
}
