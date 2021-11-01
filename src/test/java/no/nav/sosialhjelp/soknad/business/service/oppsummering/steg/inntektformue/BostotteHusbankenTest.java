package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static org.assertj.core.api.Assertions.assertThat;

class BostotteHusbankenTest {

    private final BostotteHusbanken bostotteHusbanken = new BostotteHusbanken();

    @Test
    void ikkeUtfylt() {
        var opplysninger = createOpplysninger(Collections.emptyList());

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(1);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isFalse();
        assertThat(harSoktBostotteSporsmal.getFelt()).isNull();
    }

    @Test
    void harSoktEllerMottattBostotte_manglerSamtykke() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true)
        ));

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.sporsmal.true");
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var manglerSamtykkeSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(manglerSamtykkeSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.mangler_samtykke");
        assertThat(manglerSamtykkeSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
    }

    @Test
    void harSoktEllerMottattBostotteOgSamtykke_feilMotHusbanken() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true),
                createBekreftelse(BOSTOTTE_SAMTYKKE, true)
        ));

        var driftsinformasjon = new JsonDriftsinformasjon()
                .withStotteFraHusbankenFeilet(true);
        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.sporsmal.true");
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var husbankenFeiletSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenFeiletSporsmal.getTittel()).isEqualTo("inntekt.bostotte.kontaktproblemer");
        assertThat(husbankenFeiletSporsmal.getErUtfylt()).isTrue();
    }

    @Test
    void harSoktEllerMottattBostotteOgSamtykke_medUtbetalingerOgSaker() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true),
                createBekreftelse(BOSTOTTE_SAMTYKKE, true)
        ));
        opplysninger.setUtbetaling(List.of(
                createUtbetaling(42.0, "2020-01-01"),
                createUtbetaling(1000.0, "2020-02-02")
        ));
        opplysninger.setBostotte(
                new JsonBostotte()
                        .withSaker(List.of(
                                new JsonBostotteSak()
                                        .withDato("2020-01-01")
                                        .withStatus("Vedtatt")
                                        .withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.INNVILGET)
                                        .withBeskrivelse("Ekstra info")
                        ))
        );

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.sporsmal.true");
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var husbankenUtbetalingerSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenUtbetalingerSporsmal.getErUtfylt()).isTrue();
        assertThat(husbankenUtbetalingerSporsmal.getFelt()).hasSize(4);

        var bekreftelseDato = husbankenUtbetalingerSporsmal.getFelt().get(0);
        assertThat(bekreftelseDato.getType()).isEqualTo(Type.TEKST);
        assertThat(bekreftelseDato.getSvar().getValue()).isEqualTo("2018-10-04T13:37:00.134Z");
        assertThat(bekreftelseDato.getSvar().getType()).isEqualTo(SvarType.DATO);

        var utbetaling1 = husbankenUtbetalingerSporsmal.getFelt().get(1);
        assertThat(utbetaling1.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(utbetaling1.getLabelSvarMap()).hasSize(3);
        assertThat(utbetaling1.getLabelSvarMap().get("inntekt.bostotte.utbetaling.mottaker").getValue()).isEqualTo("Husstand");
        assertThat(utbetaling1.getLabelSvarMap().get("inntekt.bostotte.utbetaling.utbetalingsdato").getValue()).isEqualTo("2020-01-01");
        assertThat(utbetaling1.getLabelSvarMap().get("inntekt.bostotte.utbetaling.belop").getValue()).isEqualTo("42.0");

        var utbetaling2 = husbankenUtbetalingerSporsmal.getFelt().get(2);
        assertThat(utbetaling2.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(utbetaling2.getLabelSvarMap()).hasSize(3);
        assertThat(utbetaling2.getLabelSvarMap().get("inntekt.bostotte.utbetaling.mottaker").getValue()).isEqualTo("Husstand");
        assertThat(utbetaling2.getLabelSvarMap().get("inntekt.bostotte.utbetaling.utbetalingsdato").getValue()).isEqualTo("2020-02-02");
        assertThat(utbetaling2.getLabelSvarMap().get("inntekt.bostotte.utbetaling.belop").getValue()).isEqualTo("1000.0");

        var sak1 = husbankenUtbetalingerSporsmal.getFelt().get(3);
        assertThat(sak1.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(sak1.getLabelSvarMap()).hasSize(2);
        assertThat(sak1.getLabelSvarMap().get("inntekt.bostotte.sak.dato").getValue()).isEqualTo("2020-01-01");
        assertThat(sak1.getLabelSvarMap().get("inntekt.bostotte.sak.status").getValue()).isEqualTo("INNVILGET: Ekstra info");
    }

    @Test
    void harSoktEllerMottattBostotteOgSamtykke_medUtbetalinger_utenSaker() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true),
                createBekreftelse(BOSTOTTE_SAMTYKKE, true)
        ));
        opplysninger.setUtbetaling(List.of(
                createUtbetaling(42.0, "2020-01-01")
        ));
        opplysninger.setBostotte(
                new JsonBostotte()
                        .withSaker(Collections.emptyList())
        );

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.sporsmal.true");
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var husbankenUtbetalingerSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenUtbetalingerSporsmal.getErUtfylt()).isTrue();
        assertThat(husbankenUtbetalingerSporsmal.getFelt()).hasSize(3);

        var bekreftelseDato = husbankenUtbetalingerSporsmal.getFelt().get(0);
        assertThat(bekreftelseDato.getType()).isEqualTo(Type.TEKST);
        assertThat(bekreftelseDato.getSvar().getValue()).isEqualTo("2018-10-04T13:37:00.134Z");
        assertThat(bekreftelseDato.getSvar().getType()).isEqualTo(SvarType.DATO);

        var utbetaling1 = husbankenUtbetalingerSporsmal.getFelt().get(1);
        assertThat(utbetaling1.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(utbetaling1.getLabelSvarMap()).hasSize(3);
        assertThat(utbetaling1.getLabelSvarMap().get("inntekt.bostotte.utbetaling.mottaker").getValue()).isEqualTo("Husstand");
        assertThat(utbetaling1.getLabelSvarMap().get("inntekt.bostotte.utbetaling.utbetalingsdato").getValue()).isEqualTo("2020-01-01");
        assertThat(utbetaling1.getLabelSvarMap().get("inntekt.bostotte.utbetaling.belop").getValue()).isEqualTo("42.0");

        var ingenSaker = husbankenUtbetalingerSporsmal.getFelt().get(2);
        assertThat(ingenSaker.getType()).isEqualTo(Type.TEKST);
        assertThat(ingenSaker.getSvar().getValue()).isEqualTo("inntekt.bostotte.sakerIkkefunnet");
        assertThat(ingenSaker.getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
    }

    @Test
    void harSoktEllerMottattBostotteOgSamtykke_utenUtbetalinger_medSaker() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true),
                createBekreftelse(BOSTOTTE_SAMTYKKE, true)
        ));
        opplysninger.setUtbetaling(Collections.emptyList());
        opplysninger.setBostotte(
                new JsonBostotte()
                        .withSaker(List.of(
                                new JsonBostotteSak()
                                        .withDato("2020-01-01")
                                        .withStatus("Vedtatt")
                                        .withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.INNVILGET)
                                        .withBeskrivelse("Ekstra info")
                        ))
        );

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.sporsmal.true");
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var husbankenUtbetalingerSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenUtbetalingerSporsmal.getErUtfylt()).isTrue();
        assertThat(husbankenUtbetalingerSporsmal.getFelt()).hasSize(3);

        var bekreftelseDato = husbankenUtbetalingerSporsmal.getFelt().get(0);
        assertThat(bekreftelseDato.getType()).isEqualTo(Type.TEKST);
        assertThat(bekreftelseDato.getSvar().getValue()).isEqualTo("2018-10-04T13:37:00.134Z");
        assertThat(bekreftelseDato.getSvar().getType()).isEqualTo(SvarType.DATO);

        var ingenUtbetalinger = husbankenUtbetalingerSporsmal.getFelt().get(1);
        assertThat(ingenUtbetalinger.getType()).isEqualTo(Type.TEKST);
        assertThat(ingenUtbetalinger.getSvar().getValue()).isEqualTo("inntekt.bostotte.utbetalingerIkkefunnet");
        assertThat(ingenUtbetalinger.getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var sak = husbankenUtbetalingerSporsmal.getFelt().get(2);
        assertThat(sak.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(sak.getLabelSvarMap()).hasSize(2);
        assertThat(sak.getLabelSvarMap().get("inntekt.bostotte.sak.dato").getValue()).isEqualTo("2020-01-01");
        assertThat(sak.getLabelSvarMap().get("inntekt.bostotte.sak.status").getValue()).isEqualTo("INNVILGET: Ekstra info");
    }

    @Test
    void harSoktEllerMottattBostotteOgSamtykke_utenUtbetalinger_utenSaker() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true),
                createBekreftelse(BOSTOTTE_SAMTYKKE, true)
        ));
        opplysninger.setUtbetaling(Collections.emptyList());
        opplysninger.setBostotte(
                new JsonBostotte()
                        .withSaker(Collections.emptyList())
        );

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.bostotte.sporsmal.true");
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var husbankenUtbetalingerSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenUtbetalingerSporsmal.getErUtfylt()).isTrue();
        assertThat(husbankenUtbetalingerSporsmal.getFelt()).hasSize(2);

        var bekreftelseDato = husbankenUtbetalingerSporsmal.getFelt().get(0);
        assertThat(bekreftelseDato.getType()).isEqualTo(Type.TEKST);
        assertThat(bekreftelseDato.getSvar().getValue()).isEqualTo("2018-10-04T13:37:00.134Z");
        assertThat(bekreftelseDato.getSvar().getType()).isEqualTo(SvarType.DATO);

        var ingenUtbetalingerEllerSaker = husbankenUtbetalingerSporsmal.getFelt().get(1);
        assertThat(ingenUtbetalingerEllerSaker.getType()).isEqualTo(Type.TEKST);
        assertThat(ingenUtbetalingerEllerSaker.getSvar().getValue()).isEqualTo("inntekt.bostotte.ikkefunnet");
        assertThat(ingenUtbetalingerEllerSaker.getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
    }

    private JsonOkonomiopplysninger createOpplysninger(List<JsonOkonomibekreftelse> bekreftelser) {
        return new JsonOkonomiopplysninger()
                .withBekreftelse(bekreftelser);
    }

    private JsonOkonomibekreftelse createBekreftelse(String type, boolean verdi) {
        return new JsonOkonomibekreftelse()
                .withType(type)
                .withVerdi(verdi)
                .withBekreftelsesDato("2018-10-04T13:37:00.134Z");
    }

    private JsonOkonomiOpplysningUtbetaling createUtbetaling(Double belop, String utbetalingsdato) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
                .withUtbetalingsdato(utbetalingsdato)
                .withNetto(belop);
    }
}
