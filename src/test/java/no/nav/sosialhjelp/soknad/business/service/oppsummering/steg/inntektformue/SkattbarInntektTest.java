package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
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
        assertThat(avsnitt.getSporsmal().get(0).getTittel()).isEqualTo("Vi fikk ikke hentet opplysninger fra Skatteetaten");
    }

    @Test
    void ingenSkattbareInntekter() {
        var okonomi = createOkonomi(true);
        var driftsinformasjon = new JsonDriftsinformasjon();

        var avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        var sporsmal = avsnitt.getSporsmal().get(0);
        assertThat(sporsmal.getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.har_gitt_samtykke");
        assertThat(sporsmal.getErUtfylt()).isTrue();
        assertThat(sporsmal.getFelt()).isEmpty();
    }

    @Test
    void harEnSkattbarInntekt() {
        var okonomi = createOkonomi(true);
        okonomi.getOpplysninger()
                .setUtbetaling(List.of(
                        createUtbetaling("2020-01-01", "2020-02-01", 1234d, 123d)
                        )
                );
        var driftsinformasjon = new JsonDriftsinformasjon();

        var avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        var sporsmal = avsnitt.getSporsmal().get(0);
        assertThat(sporsmal.getTittel()).isEqualTo("utbetalinger.inntekt.skattbar.har_gitt_samtykke");
        assertThat(sporsmal.getErUtfylt()).isTrue();
        assertThat(sporsmal.getFelt()).hasSize(1);
        var felt = sporsmal.getFelt().get(0);
        assertThat(felt.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(felt.getLabelSvarMap())
                .hasSize(5)
                .containsEntry("utbetalinger.utbetaling.arbeidsgivernavn.label", "arbeidsgiver")
                .containsEntry("utbetalinger.utbetaling.periodeFom.label", "2020-01-01")
                .containsEntry("utbetalinger.utbetaling.periodeTom.label", "2020-02-01")
                .containsEntry("utbetalinger.utbetaling.brutto.label", "1234.0")
                .containsEntry("utbetalinger.utbetaling.skattetrekk.label", "123.0");
    }

    @Test
    void skalGruppereSkattbareInntekter() {
        // todo
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