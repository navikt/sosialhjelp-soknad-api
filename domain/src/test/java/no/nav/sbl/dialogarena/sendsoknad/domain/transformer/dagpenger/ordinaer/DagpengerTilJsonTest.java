package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import junit.framework.AssertionFailedError;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;

public class DagpengerTilJsonTest {


    private JsonDagpengerSoknad jsonSoknad;

    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad()
                .medId(1234)
                .medskjemaNummer("NAV 04-01.03")
                .medStatus(UNDER_ARBEID)
                .medAktorId("12345")
                .medDelstegStatus(DelstegStatus.VEDLEGG_VALIDERT)
                .medSoknadPrefix("dagpenger.ordinaer")
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villighelse").medValue("false"))
                .medFaktum(new Faktum().medKey("arbeidsforhold.arbeidstilstand").medValue("varierendeArbeidstid"))
                .medFaktum(new Faktum().medKey("egennaering.driveregennaering").medValue("true"))
                .medFaktum(new Faktum().medKey("arbeidsforhold").medValue(null)
                        .medProperty("arbeidsgivernavn", "Et firma")
                        .medProperty("type", "kontraktutgaatt"))
                .medVedlegg(new Vedlegg()
                        .medSkjemaNummer("02")
                        .medSkjemanummerTillegg("kontraktutgaatt")
                        .medInnsendingsvalg(LastetOpp)
                        .medNavn("Navn på vedlegg")
                        .medStorrelse(12345L)
                        .medAntallSider(1));

        jsonSoknad = new DagpengerTilJson("dagpenger.ordinaer").transform(soknad);

        assertThat(jsonSoknad.getSoknadsType()).isEqualTo("dagpenger.ordinaer");
        assertThat(jsonSoknad.getSkjemaNummer()).isEqualTo("NAV 04-01.03");
        assertThat(jsonSoknad.getAktoerId()).isEqualTo("12345");
        assertThat(jsonSoknad.getStatus()).isEqualTo(UNDER_ARBEID);
        assertThat(jsonSoknad.getSoknadPrefix()).isEqualTo("dagpenger.ordinaer");
        assertThat(jsonSoknad.getFakta()).hasSize(4);
        assertThat(getFaktum("reellarbeidssoker.villighelse").getValue()).isEqualTo("false");
        assertThat(getFaktum("arbeidsforhold.arbeidstilstand").getValue()).isEqualTo(("varierendeArbeidstid"));
        assertThat(getFaktum("egennaering.driveregennaering").getValue()).isEqualTo(("true"));
        assertThat(getFaktum("arbeidsforhold").getProperties().get("arbeidsgivernavn")).isEqualTo("Et firma");
        assertThat(getFaktum("arbeidsforhold").getProperties().get("type")).isEqualTo("kontraktutgaatt");
        assertThat(jsonSoknad.getVedlegg()).hasSize(1);
        assertThat(jsonSoknad.getVedlegg().get(0).getSkjemaNummer()).isEqualTo("02");
        assertThat(jsonSoknad.getVedlegg().get(0).getSkjemanummerTillegg()).isEqualTo("kontraktutgaatt");
        assertThat(jsonSoknad.getVedlegg().get(0).getInnsendingsvalg()).isEqualTo(LastetOpp);
        assertThat(jsonSoknad.getVedlegg().get(0).getNavn()).isEqualTo("Navn på vedlegg");
        assertThat(jsonSoknad.getVedlegg().get(0).getAntallSider()).isEqualTo(1);
    }


    private JsonDagpengerFaktum getFaktum(String key) {
        return jsonSoknad
                .getFakta()
                .stream()
                .filter(jsonDagpengerFaktum -> jsonDagpengerFaktum.getKey().equals(key))
                .findFirst()
                .orElseThrow(AssertionFailedError::new);

    }

}