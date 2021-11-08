package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class SkattbarInntektTest {

    private final SkattbarInntekt skattbarInntekt = new SkattbarInntekt();

    @Test
    void manglerSamtykke() {
        var okonomi = createOkonomi(false);
        var driftsinformasjon = new JsonDriftsinformasjon();

        var avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        assertThat(avsnitt.getSporsmal().get(0).getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.mangler_samtykke");
    }

    @Test
    void feilMotSkatteetaten() {
        var okonomi = createOkonomi(true);
        var driftsinformasjon = new JsonDriftsinformasjon().withInntektFraSkatteetatenFeilet(true);

        var avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        assertThat(avsnitt.getSporsmal().get(0).getTittel()).isEqualTo("utbetalinger.skattbar.kontaktproblemer.oppsummering");
    }

    @Test
    void ingenSkattbareInntekter() {
        var okonomi = createOkonomi(true);
        var driftsinformasjon = new JsonDriftsinformasjon();

        var avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var bekreftelseTidspunktSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(bekreftelseTidspunktSporsmal.getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.har_gitt_samtykke");
        assertThat(bekreftelseTidspunktSporsmal.getErUtfylt()).isTrue();
        assertThat(bekreftelseTidspunktSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.getFelt().get(0), Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z");

        var inntekterSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(inntekterSporsmal.getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.inntekt.tittel");
        assertThat(inntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(inntekterSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(inntekterSporsmal.getFelt().get(0), Type.TEKST, SvarType.LOCALE_TEKST, "utbetalinger.inntekt.skattbar.ingen");
    }

    @Test
    void harEnSkattbarInntekt() {
        var okonomi = createOkonomi(true);
        okonomi.getOpplysninger()
                .setUtbetaling(
                        List.of(createUtbetaling("2020-01-01", "2020-02-01", 1234d, 123d))
                );
        var driftsinformasjon = new JsonDriftsinformasjon();

        var avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var bekreftelseTidspunktSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(bekreftelseTidspunktSporsmal.getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.har_gitt_samtykke");
        assertThat(bekreftelseTidspunktSporsmal.getErUtfylt()).isTrue();
        assertThat(bekreftelseTidspunktSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.getFelt().get(0), Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z");

        var inntekterSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(inntekterSporsmal.getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.inntekt.tittel");
        assertThat(inntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(inntekterSporsmal.getFelt()).hasSize(1);
        var inntekt = inntekterSporsmal.getFelt().get(0);
        assertThat(inntekt.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(inntekt.getLabelSvarMap()).hasSize(5);
        assertThat(inntekt.getLabelSvarMap().get("utbetalinger.utbetaling.arbeidsgivernavn.label").getValue()).isEqualTo("arbeidsgiver");
        assertThat(inntekt.getLabelSvarMap().get("utbetalinger.utbetaling.periodeFom.label").getValue()).isEqualTo("2020-01-01");
        assertThat(inntekt.getLabelSvarMap().get("utbetalinger.utbetaling.periodeTom.label").getValue()).isEqualTo("2020-02-01");
        assertThat(inntekt.getLabelSvarMap().get("utbetalinger.utbetaling.brutto.label").getValue()).isEqualTo("1234.0");
        assertThat(inntekt.getLabelSvarMap().get("utbetalinger.utbetaling.skattetrekk.label").getValue()).isEqualTo("123.0");
    }

    private JsonOkonomi createOkonomi(boolean harSamtykke) {
        return new JsonOkonomi()
                .withOpplysninger(
                        new JsonOkonomiopplysninger()
                                .withBekreftelse(
                                        singletonList(
                                                new JsonOkonomibekreftelse()
                                                        .withType(UTBETALING_SKATTEETATEN_SAMTYKKE)
                                                        .withVerdi(harSamtykke)
                                                        .withBekreftelsesDato("2018-10-04T13:37:00.134Z")
                                        )
                                )
                );
    }

    private JsonOkonomiOpplysningUtbetaling createUtbetaling(String fom, String tom, Double brutto, Double skattetrekk) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withType(UTBETALING_SKATTEETATEN)
                .withKilde(JsonKilde.SYSTEM)
                .withOrganisasjon(new JsonOrganisasjon().withNavn("arbeidsgiver"))
                .withPeriodeFom(fom)
                .withPeriodeTom(tom)
                .withBrutto(brutto)
                .withSkattetrekk(skattetrekk);
    }
}