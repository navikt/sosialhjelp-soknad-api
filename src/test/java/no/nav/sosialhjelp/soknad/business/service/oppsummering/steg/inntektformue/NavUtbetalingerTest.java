package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static org.assertj.core.api.Assertions.assertThat;

class NavUtbetalingerTest {
    
    private final NavUtbetalinger navUtbetalinger = new NavUtbetalinger();

    @Test
    void hentingFeilet() {
        var opplysninger = new JsonOkonomiopplysninger();
        var driftsinformasjon = new JsonDriftsinformasjon()
                .withUtbetalingerFraNavFeilet(true);

        var avsnitt = navUtbetalinger.getAvsnitt(opplysninger, driftsinformasjon);

        assertThat(avsnitt.getSporsmal()).hasSize(1);

        var sporsmal = avsnitt.getSporsmal().get(0);
        assertThat(sporsmal.getTittel()).isEqualTo("utbetalinger.kontaktproblemer");
        assertThat(sporsmal.getErUtfylt()).isTrue();
    }

    @Test
    void ingenNavUtbetalinger() {
        var opplysninger = new JsonOkonomiopplysninger()
                .withUtbetaling(Collections.emptyList());

        var avsnitt = navUtbetalinger.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(1);

        var sporsmal = avsnitt.getSporsmal().get(0);
        assertThat(sporsmal.getTittel()).isEqualTo("utbetalinger.ingen.true");
        assertThat(sporsmal.getErUtfylt()).isTrue();
        assertThat(sporsmal.getFelt()).isNull();
    }

    @Test
    void flereNavUtbetalinger() {
        var opplysninger = new JsonOkonomiopplysninger()
                .withUtbetaling(List.of(
                        createUtbetaling("Dagpenger", 1234.0, "2021-01-01"),
                        createUtbetaling("Uføre", 42.0, "2021-03-03")
                ));

        var avsnitt = navUtbetalinger.getAvsnitt(opplysninger, new JsonDriftsinformasjon());

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var sporsmalUtbetaling1 = avsnitt.getSporsmal().get(0);
        assertThat(sporsmalUtbetaling1.getTittel()).isEqualTo("utbetalinger.utbetaling.sporsmal");
        assertThat(sporsmalUtbetaling1.getErUtfylt()).isTrue();
        assertThat(sporsmalUtbetaling1.getFelt()).hasSize(1);
        assertThat(sporsmalUtbetaling1.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        var labelSvarMap1 = sporsmalUtbetaling1.getFelt().get(0).getLabelSvarMap();
        assertThat(labelSvarMap1).hasSize(4);
        assertThat(labelSvarMap1.get("utbetalinger.utbetaling.type.label").getValue()).isEqualTo("Dagpenger");
        assertThat(labelSvarMap1.get("utbetalinger.utbetaling.netto.label").getValue()).isEqualTo("1234.0");
        assertThat(labelSvarMap1.get("utbetalinger.utbetaling.brutto.label").getValue()).isEqualTo("2234.0");
        assertThat(labelSvarMap1.get("utbetalinger.utbetaling.utbetalingsdato.label").getValue()).isEqualTo("2021-01-01");

        var sporsmalUtbetaling2 = avsnitt.getSporsmal().get(1);
        assertThat(sporsmalUtbetaling2.getTittel()).isEqualTo("utbetalinger.utbetaling.sporsmal");
        assertThat(sporsmalUtbetaling2.getErUtfylt()).isTrue();
        assertThat(sporsmalUtbetaling2.getFelt()).hasSize(1);
        assertThat(sporsmalUtbetaling2.getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        var labelSvarMap2 = sporsmalUtbetaling2.getFelt().get(0).getLabelSvarMap();
        assertThat(labelSvarMap2).hasSize(4);
        assertThat(labelSvarMap2.get("utbetalinger.utbetaling.type.label").getValue()).isEqualTo("Uføre");
        assertThat(labelSvarMap2.get("utbetalinger.utbetaling.netto.label").getValue()).isEqualTo("42.0");
        assertThat(labelSvarMap2.get("utbetalinger.utbetaling.brutto.label").getValue()).isEqualTo("1042.0");
        assertThat(labelSvarMap2.get("utbetalinger.utbetaling.utbetalingsdato.label").getValue()).isEqualTo("2021-03-03");
    }

    private JsonOkonomiOpplysningUtbetaling createUtbetaling(String tittel, Double netto, String utbetalingsdato) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withType(UTBETALING_NAVYTELSE)
                .withKilde(JsonKilde.SYSTEM)
                .withTittel(tittel)
                .withNetto(netto)
                .withBrutto(netto + 1000)
                .withUtbetalingsdato(utbetalingsdato);
    }
}
