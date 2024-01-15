package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BostotteHusbankenTest {

    private val bostotteHusbanken = BostotteHusbanken()

    @Test
    fun ikkeUtfylt() {
        val opplysninger = createOpplysninger(emptyList())

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(1)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")
        assertThat(harSoktBostotteSporsmal.erUtfylt).isFalse
        assertThat(harSoktBostotteSporsmal.felt).isNull()
    }

    @Test
    fun harSoktEllerMottattBostotte_manglerSamtykke() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
            ),
        )

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(2)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")
        assertThat(harSoktBostotteSporsmal.erUtfylt).isTrue
        assertThat(harSoktBostotteSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harSoktBostotteSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bostotte.sporsmal.true")

        val manglerSamtykkeSporsmal = avsnitt.sporsmal[1]
        assertThat(manglerSamtykkeSporsmal.tittel).isEqualTo("inntekt.bostotte.mangler_samtykke")
        assertThat(manglerSamtykkeSporsmal.felt).isNull()
    }

    @Test
    fun harSoktEllerMottattBostotteOgSamtykke_feilMotHusbanken() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
                createBekreftelse(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true),
            ),
        )
        val driftsinformasjon = JsonDriftsinformasjon().withStotteFraHusbankenFeilet(true)

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, driftsinformasjon)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")
        assertThat(harSoktBostotteSporsmal.erUtfylt).isTrue
        assertThat(harSoktBostotteSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harSoktBostotteSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bostotte.sporsmal.true")

        val husbankenFeiletSporsmal = avsnitt.sporsmal[1]
        assertThat(husbankenFeiletSporsmal.tittel).isEqualTo("inntekt.bostotte.kontaktproblemer")
        assertThat(husbankenFeiletSporsmal.erUtfylt).isTrue
    }

    @Test
    fun harSoktEllerMottattBostotteOgSamtykke_medUtbetalinger_medSaker() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
                createBekreftelse(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true),
            ),
        )
        opplysninger.utbetaling = listOf(
            createUtbetaling(42.0, "2020-01-01"),
            createUtbetaling(1000.0, "2020-02-02"),
        )
        opplysninger.bostotte = JsonBostotte()
            .withSaker(
                listOf(
                    JsonBostotteSak()
                        .withDato("2020-01-01")
                        .withStatus("Vedtatt")
                        .withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.INNVILGET)
                        .withBeskrivelse("Ekstra info"),
                ),
            )

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(4)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")
        assertThat(harSoktBostotteSporsmal.erUtfylt).isTrue
        assertThat(harSoktBostotteSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harSoktBostotteSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bostotte.sporsmal.true")

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[1]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("inntekt.bostotte.har_gitt_samtykke")
        assertThat(bekreftelseTidspunktSporsmal.felt).hasSize(1)
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.felt!![0], Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z")

        val utbetalingerSporsmal = avsnitt.sporsmal[2]
        assertThat(utbetalingerSporsmal.tittel).isEqualTo("inntekt.bostotte.utbetaling")
        assertThat(utbetalingerSporsmal.felt).hasSize(2)

        val utbetaling1 = utbetalingerSporsmal.felt!![0]
        assertThat(utbetaling1.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(utbetaling1.labelSvarMap).hasSize(3)
        assertThat(utbetaling1.labelSvarMap!!["inntekt.bostotte.utbetaling.mottaker"]!!.value).isEqualTo("Husstand")
        assertThat(utbetaling1.labelSvarMap!!["inntekt.bostotte.utbetaling.utbetalingsdato"]!!.value).isEqualTo("2020-01-01")
        assertThat(utbetaling1.labelSvarMap!!["inntekt.bostotte.utbetaling.belop"]!!.value).isEqualTo("42.0")

        val utbetaling2 = utbetalingerSporsmal.felt!![1]
        assertThat(utbetaling2.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(utbetaling2.labelSvarMap).hasSize(3)
        assertThat(utbetaling2.labelSvarMap!!["inntekt.bostotte.utbetaling.mottaker"]!!.value).isEqualTo("Husstand")
        assertThat(utbetaling2.labelSvarMap!!["inntekt.bostotte.utbetaling.utbetalingsdato"]!!.value).isEqualTo("2020-02-02")
        assertThat(utbetaling2.labelSvarMap!!["inntekt.bostotte.utbetaling.belop"]!!.value).isEqualTo("1000.0")

        val sakerSporsmal = avsnitt.sporsmal[3]
        assertThat(sakerSporsmal.tittel).isEqualTo("inntekt.bostotte.sak")
        assertThat(sakerSporsmal.felt).hasSize(1)

        val sak1 = sakerSporsmal.felt!![0]
        assertThat(sak1.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(sak1.labelSvarMap).hasSize(2)
        assertThat(sak1.labelSvarMap!!["inntekt.bostotte.sak.dato"]!!.value).isEqualTo("2020-01-01")
        assertThat(sak1.labelSvarMap!!["inntekt.bostotte.sak.status"]!!.value).isEqualTo("INNVILGET: Ekstra info")
    }

    @Test
    fun harSoktEllerMottattBostotteOgSamtykke_medUtbetalinger_utenSaker() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
                createBekreftelse(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true),
            ),
        )
        opplysninger.utbetaling = listOf(createUtbetaling(42.0, "2020-01-01"))
        opplysninger.bostotte = JsonBostotte()

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(4)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")
        assertThat(harSoktBostotteSporsmal.erUtfylt).isTrue
        assertThat(harSoktBostotteSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harSoktBostotteSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bostotte.sporsmal.true")

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[1]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("inntekt.bostotte.har_gitt_samtykke")
        assertThat(bekreftelseTidspunktSporsmal.felt).hasSize(1)
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.felt!![0], Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z")

        val utbetalingerSporsmal = avsnitt.sporsmal[2]
        assertThat(utbetalingerSporsmal.erUtfylt).isTrue
        assertThat(utbetalingerSporsmal.felt).hasSize(1)

        val utbetaling = utbetalingerSporsmal.felt!![0]
        assertThat(utbetaling.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(utbetaling.labelSvarMap).hasSize(3)
        assertThat(utbetaling.labelSvarMap!!["inntekt.bostotte.utbetaling.mottaker"]!!.value).isEqualTo("Husstand")
        assertThat(utbetaling.labelSvarMap!!["inntekt.bostotte.utbetaling.utbetalingsdato"]!!.value).isEqualTo("2020-01-01")
        assertThat(utbetaling.labelSvarMap!!["inntekt.bostotte.utbetaling.belop"]!!.value).isEqualTo("42.0")

        val sakerSporsmal = avsnitt.sporsmal[3]
        assertThat(sakerSporsmal.tittel).isEqualTo("inntekt.bostotte.sak")
        assertThat(sakerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(sakerSporsmal.felt!![0], Type.TEKST, SvarType.LOCALE_TEKST, "inntekt.bostotte.sakerIkkefunnet")
    }

    @Test
    fun nullsafe_utbetaling_mottaker() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
                createBekreftelse(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true),
            ),
        )
        opplysninger.utbetaling = listOf(
            createUtbetaling(42.0, "2020-01-01")
                .withMottaker(null),
        )
        opplysninger.bostotte = JsonBostotte()

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(4)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[1]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("inntekt.bostotte.har_gitt_samtykke")

        val utbetalingerSporsmal = avsnitt.sporsmal[2]
        assertThat(utbetalingerSporsmal.tittel).isEqualTo("inntekt.bostotte.utbetaling")
        assertThat(utbetalingerSporsmal.felt).hasSize(1)

        val utbetaling = utbetalingerSporsmal.felt!![0]
        assertThat(utbetaling.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(utbetaling.labelSvarMap!!["inntekt.bostotte.utbetaling.mottaker"]!!.value).isEmpty()

        val sakerSporsmal = avsnitt.sporsmal[3]
        assertThat(sakerSporsmal.tittel).isEqualTo("inntekt.bostotte.sak")
    }

    @Test
    fun harSoktEllerMottattBostotteOgSamtykke_utenUtbetalinger_medSaker() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
                createBekreftelse(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true),
            ),
        )
        opplysninger.utbetaling = emptyList()
        opplysninger.bostotte = JsonBostotte()
            .withSaker(
                listOf(
                    JsonBostotteSak()
                        .withDato("2020-01-01")
                        .withStatus("Vedtatt")
                        .withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.INNVILGET)
                        .withBeskrivelse("Ekstra info"),
                ),
            )

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(4)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.tittel).isEqualTo("inntekt.bostotte.sporsmal.sporsmal")
        assertThat(harSoktBostotteSporsmal.erUtfylt).isTrue
        assertThat(harSoktBostotteSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harSoktBostotteSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bostotte.sporsmal.true")

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[1]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("inntekt.bostotte.har_gitt_samtykke")
        assertThat(bekreftelseTidspunktSporsmal.felt).hasSize(1)
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.felt!![0], Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z")

        val utbetalingerSporsmal = avsnitt.sporsmal[2]
        assertThat(utbetalingerSporsmal.tittel).isEqualTo("inntekt.bostotte.utbetaling")
        assertThat(utbetalingerSporsmal.erUtfylt).isTrue
        assertThat(utbetalingerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(utbetalingerSporsmal.felt!![0], Type.TEKST, SvarType.LOCALE_TEKST, "inntekt.bostotte.utbetalingerIkkefunnet")

        val sakerSporsmal = avsnitt.sporsmal[3]
        assertThat(sakerSporsmal.tittel).isEqualTo("inntekt.bostotte.sak")
        assertThat(sakerSporsmal.felt).hasSize(1)

        val sak = sakerSporsmal.felt!![0]
        assertThat(sak.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(sak.labelSvarMap).hasSize(2)
        assertThat(sak.labelSvarMap!!["inntekt.bostotte.sak.dato"]!!.value).isEqualTo("2020-01-01")
        assertThat(sak.labelSvarMap!!["inntekt.bostotte.sak.status"]!!.value).isEqualTo("INNVILGET: Ekstra info")
    }

    @Test
    fun harSoktEllerMottattBostotteOgSamtykke_utenUtbetalinger_utenSaker() {
        val opplysninger = createOpplysninger(
            listOf(
                createBekreftelse(SoknadJsonTyper.BOSTOTTE, true),
                createBekreftelse(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true),
            ),
        )
        opplysninger.utbetaling = emptyList()
        opplysninger.bostotte = JsonBostotte()

        val avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(3)

        val harSoktBostotteSporsmal = avsnitt.sporsmal[0]
        assertThat(harSoktBostotteSporsmal.erUtfylt).isTrue
        assertThat(harSoktBostotteSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harSoktBostotteSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bostotte.sporsmal.true")

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[1]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("inntekt.bostotte.har_gitt_samtykke")
        assertThat(bekreftelseTidspunktSporsmal.felt).hasSize(1)
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.felt!![0], Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z")

        val ingenUtbetalingerEllerSakerSporsmal = avsnitt.sporsmal[2]
        assertThat(ingenUtbetalingerEllerSakerSporsmal.tittel).isEmpty()
        assertThat(ingenUtbetalingerEllerSakerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(ingenUtbetalingerEllerSakerSporsmal.felt!![0], Type.TEKST, SvarType.LOCALE_TEKST, "inntekt.bostotte.ikkefunnet")
    }

    private fun createOpplysninger(bekreftelser: List<JsonOkonomibekreftelse>): JsonOkonomiopplysninger {
        return JsonOkonomiopplysninger()
            .withBekreftelse(bekreftelser)
    }

    private fun createBekreftelse(type: String, verdi: Boolean): JsonOkonomibekreftelse {
        return JsonOkonomibekreftelse()
            .withType(type)
            .withVerdi(verdi)
            .withBekreftelsesDato("2018-10-04T13:37:00.134Z")
    }

    private fun createUtbetaling(belop: Double, utbetalingsdato: String): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.SYSTEM)
            .withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)
            .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
            .withUtbetalingsdato(utbetalingsdato)
            .withNetto(belop)
    }
}
