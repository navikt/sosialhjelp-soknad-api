package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
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
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar()).isEqualTo("inntekt.bostotte.sporsmal.true");

        var manglerSamtykkeSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(manglerSamtykkeSporsmal.getFelt().get(0).getSvar()).isEqualTo("inntekt.bostotte.mangler_samtykke");
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
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar()).isEqualTo("inntekt.bostotte.sporsmal.true");

        var husbankenFeiletSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenFeiletSporsmal.getTittel()).isEqualTo("Vi fikk ikke hentet opplysninger fra Husbanken");
    }

    @Test
    void harSoktEllerMottattBostotteOgSamtykke_medUtbetalingerOgSaker() {
        var opplysninger = createOpplysninger(List.of(
                createBekreftelse(BOSTOTTE, true),
                createBekreftelse(BOSTOTTE_SAMTYKKE, true)
        ));
        opplysninger.setUtbetaling(List.of(
                createUtbetaling(42, "2020-01-01"),
                createUtbetaling(1000, "2020-02-02")
        ));
        opplysninger.setBostotte(
                new JsonBostotte()
                        .withSaker(List.of(
                                new JsonBostotteSak()
                                        .withDato("2020-01-01")
                                        .withStatus("Under behandling")
                        ))
        );

        var avsnitt = bostotteHusbanken.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harSoktBostotteSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harSoktBostotteSporsmal.getErUtfylt()).isTrue();
        assertThat(harSoktBostotteSporsmal.getFelt()).hasSize(1);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harSoktBostotteSporsmal.getFelt().get(0).getSvar()).isEqualTo("inntekt.bostotte.sporsmal.true");

        var husbankenUtbetalingerSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(husbankenUtbetalingerSporsmal.getErUtfylt()).isTrue();
        assertThat(husbankenUtbetalingerSporsmal.getFelt()).hasSize(3);

        var utbetaling1 = husbankenUtbetalingerSporsmal.getFelt().get(0);
        assertThat(utbetaling1.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(utbetaling1.getLabelSvarMap())
                .hasSize(3)
                .containsEntry("inntekt.bostotte.utbetaling.mottaker", "Husstand")
                .containsEntry("inntekt.bostotte.utbetaling.utbetalingsdato", "2020-01-01")
                .containsEntry("inntekt.bostotte.utbetaling.belop", "42");

        var utbetaling2 = husbankenUtbetalingerSporsmal.getFelt().get(1);
        assertThat(utbetaling2.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(utbetaling2.getLabelSvarMap())
                .hasSize(3)
                .containsEntry("inntekt.bostotte.utbetaling.mottaker", "Husstand")
                .containsEntry("inntekt.bostotte.utbetaling.utbetalingsdato", "2020-02-02")
                .containsEntry("inntekt.bostotte.utbetaling.belop", "1000");

        var sak1 = husbankenUtbetalingerSporsmal.getFelt().get(2);
        assertThat(sak1.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(sak1.getLabelSvarMap())
                .hasSize(2)
                .containsEntry("inntekt.bostotte.sak.dato", "2020-01-01")
                .containsEntry("inntekt.bostotte.sak.status", "Under behandling");
    }

    private JsonOkonomiopplysninger createOpplysninger(List<JsonOkonomibekreftelse> bekreftelser) {
        return new JsonOkonomiopplysninger()
                .withBekreftelse(bekreftelser);
    }

    private JsonOkonomibekreftelse createBekreftelse(String type, boolean verdi) {
        return new JsonOkonomibekreftelse().withType(type).withVerdi(verdi);
    }

    private JsonOkonomiOpplysningUtbetaling createUtbetaling(Integer belop, String utbetalingsdato) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
                .withUtbetalingsdato(utbetalingsdato)
                .withBelop(belop);
    }
}
