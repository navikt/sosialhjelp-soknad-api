package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

class NavUtbetalinger {

    fun getAvsnitt(opplysninger: JsonOkonomiopplysninger, driftsinformasjon: JsonDriftsinformasjon): Avsnitt {
        val utbetalingerFraNavFeilet = java.lang.Boolean.TRUE == driftsinformasjon.utbetalingerFraNavFeilet
        return Avsnitt(
            tittel = "navytelser.sporsmal",
            sporsmal = navUtbetalingerSporsmal(opplysninger, utbetalingerFraNavFeilet)
        )
    }

    private fun navUtbetalingerSporsmal(
        opplysninger: JsonOkonomiopplysninger,
        utbetalingerFraNavFeilet: Boolean
    ): List<Sporsmal> {
        if (utbetalingerFraNavFeilet) {
            // På grunn av systemfeil klarte vi ikke å hente ned informasjon om ytelser fra NAV.
            return listOf(
                Sporsmal(
                    tittel = "utbetalinger.kontaktproblemer",
                    erUtfylt = true,
                    felt = null
                )
            )
        }
        val harNavUtbetalinger =
            opplysninger.utbetaling != null && opplysninger.utbetaling.any { UTBETALING_NAVYTELSE == it.type }
        return if (!harNavUtbetalinger) {
            // Vi har ingen registrerte utbetalinger på deg fra NAV den siste måneden.
            listOf(
                Sporsmal(
                    tittel = "utbetalinger.ingen.true",
                    erUtfylt = true,
                    felt = null
                )
            )
        } else { // 1 eller flere utbetalinger
            opplysninger.utbetaling
                .filter { UTBETALING_NAVYTELSE == it.type }
                .map {
                    val map = LinkedHashMap<String, Svar>()
                    map["utbetalinger.utbetaling.type.label"] = createSvar(it.tittel, SvarType.TEKST)
                    map["utbetalinger.utbetaling.netto.label"] = createSvar(it.netto.toString(), SvarType.TEKST)
                    map["utbetalinger.utbetaling.brutto.label"] = createSvar(it.brutto.toString(), SvarType.TEKST)
                    map["utbetalinger.utbetaling.utbetalingsdato.label"] = createSvar(it.utbetalingsdato, SvarType.DATO)
                    Sporsmal(
                        tittel = "utbetalinger.utbetaling.sporsmal",
                        erUtfylt = true,
                        felt = listOf(
                            Felt(
                                type = Type.SYSTEMDATA_MAP,
                                labelSvarMap = map
                            )
                        )
                    )
                }
        }
    }
}
