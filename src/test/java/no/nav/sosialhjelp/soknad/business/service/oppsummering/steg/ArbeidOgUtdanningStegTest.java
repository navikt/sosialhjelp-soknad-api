package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class ArbeidOgUtdanningStegTest {

    private final ArbeidOgUtdanningSteg steg = new ArbeidOgUtdanningSteg();

    private final JsonArbeidsforhold arbeidsforholdMedSlutt = new JsonArbeidsforhold().withArbeidsgivernavn("arbeidsgiver").withFom("01.01.2021").withTom("10.10.2021").withStillingsprosent(100);
    private final JsonArbeidsforhold arbeidsforholdUtenSlutt = new JsonArbeidsforhold().withArbeidsgivernavn("arbeidsgiver2").withFom("01.01.2021").withStillingsprosent(100);

    private final JsonUtdanning ikkeStudent = new JsonUtdanning().withErStudent(false);
    private final JsonUtdanning studentUtenStudentgrad = new JsonUtdanning().withErStudent(true);
    private final JsonUtdanning heltidstudent = new JsonUtdanning().withErStudent(true).withStudentgrad(JsonUtdanning.Studentgrad.HELTID);

    @Test
    void ingenArbeidsforhold() {
        var soknad = createSoknad(new JsonArbeid(), new JsonUtdanning());

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        var arbeidsforholdSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(arbeidsforholdSporsmal.getTittel()).isEqualTo("arbeidsforhold.ingen");
        assertThat(arbeidsforholdSporsmal.getErUtfylt()).isTrue();
        assertThat(arbeidsforholdSporsmal.getFelt()).isNull();
    }

    @Test
    void arbeidsforholdMedSlutt() {
        var soknad = createSoknad(new JsonArbeid().withForhold(singletonList(arbeidsforholdMedSlutt)), new JsonUtdanning());

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);

        var arbeidsforholdSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(arbeidsforholdSporsmal.getTittel()).isEqualTo("arbeidsforhold.infotekst");
        assertThat(arbeidsforholdSporsmal.getErUtfylt()).isTrue();
        assertThat(arbeidsforholdSporsmal.getFelt()).hasSize(1);

        var felt = arbeidsforholdSporsmal.getFelt().get(0);
        assertThat(felt.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(felt.getLabelSvarMap()).hasSize(4);

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.arbeidsgivernavn.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.arbeidsgivernavn.label").getValue()).isEqualTo(arbeidsforholdMedSlutt.getArbeidsgivernavn());

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.fom.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.fom.label").getValue()).isEqualTo(arbeidsforholdMedSlutt.getFom());

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.tom.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.tom.label").getValue()).isEqualTo(arbeidsforholdMedSlutt.getTom());

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.stillingsprosent.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.stillingsprosent.label").getValue()).isEqualTo(arbeidsforholdMedSlutt.getStillingsprosent().toString());
    }

    @Test
    void arbeidsforholdUtenSlutt() {
        var soknad = createSoknad(new JsonArbeid().withForhold(singletonList(arbeidsforholdUtenSlutt)), new JsonUtdanning());

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        var arbeidsforholdSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(arbeidsforholdSporsmal.getTittel()).isEqualTo("arbeidsforhold.infotekst");
        assertThat(arbeidsforholdSporsmal.getErUtfylt()).isTrue();
        assertThat(arbeidsforholdSporsmal.getFelt()).hasSize(1);

        var felt = arbeidsforholdSporsmal.getFelt().get(0);
        assertThat(felt.getType()).isEqualTo(Type.SYSTEMDATA_MAP);
        assertThat(felt.getLabelSvarMap()).hasSize(3);

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.arbeidsgivernavn.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.arbeidsgivernavn.label").getValue()).isEqualTo(arbeidsforholdUtenSlutt.getArbeidsgivernavn());

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.fom.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.fom.label").getValue()).isEqualTo(arbeidsforholdUtenSlutt.getFom());

        assertThat(felt.getLabelSvarMap()).doesNotContainKey("arbeidsforhold.tom.label");

        assertThat(felt.getLabelSvarMap()).containsKey("arbeidsforhold.stillingsprosent.label");
        assertThat(felt.getLabelSvarMap().get("arbeidsforhold.stillingsprosent.label").getValue()).isEqualTo(arbeidsforholdUtenSlutt.getStillingsprosent().toString());
    }

    @Test
    void arbeidsforholdMedKommentar() {
        var soknad = createSoknad(new JsonArbeid().withForhold(singletonList(arbeidsforholdUtenSlutt)).withKommentarTilArbeidsforhold(new JsonKommentarTilArbeidsforhold().withVerdi("kommentar")), new JsonUtdanning());

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);
        var arbeidsforholdSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(arbeidsforholdSporsmal.getTittel()).isEqualTo("arbeidsforhold.infotekst");
        assertThat(arbeidsforholdSporsmal.getErUtfylt()).isTrue();

        var arbeidsforholdKommentarSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(arbeidsforholdKommentarSporsmal.getTittel()).isEqualTo("opplysninger.arbeidsituasjon.kommentarer.label");
        assertThat(arbeidsforholdKommentarSporsmal.getErUtfylt()).isTrue();
        assertThat(arbeidsforholdKommentarSporsmal.getFelt()).hasSize(1);
        assertThat(arbeidsforholdKommentarSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("kommentar");
        assertThat(arbeidsforholdKommentarSporsmal.getFelt().get(0).getType()).isEqualTo(Type.TEKST);
    }

    @Test
    void ikkeUtfyltStudent() {
        var soknad = createSoknad(new JsonArbeid(), new JsonUtdanning());

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(1).getSporsmal()).hasSize(1);
        var utdanningSporsmal = res.getAvsnitt().get(1).getSporsmal().get(0);
        assertThat(utdanningSporsmal.getTittel()).isEqualTo("dinsituasjon.studerer.sporsmal");
        assertThat(utdanningSporsmal.getErUtfylt()).isFalse();
        assertThat(utdanningSporsmal.getFelt()).isNull();
    }

    @Test
    void erIkkeStudent() {
        var soknad = createSoknad(new JsonArbeid(), ikkeStudent);

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(2);
        assertThat(res.getAvsnitt().get(1).getSporsmal()).hasSize(1);
        var utdanningSporsmal = res.getAvsnitt().get(1).getSporsmal().get(0);
        assertThat(utdanningSporsmal.getErUtfylt()).isTrue();
        assertThat(utdanningSporsmal.getFelt()).hasSize(1);
        assertThat(utdanningSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("dinsituasjon.studerer.false");
        assertThat(utdanningSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
        assertThat(utdanningSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
    }

    @Test
    void ikkeUtfyltStudentgrad() {
        var soknad = createSoknad(new JsonArbeid(), studentUtenStudentgrad);

        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt().get(1).getSporsmal()).hasSize(2);

        var utdanningSporsmal = res.getAvsnitt().get(1).getSporsmal().get(0);
        assertThat(utdanningSporsmal.getErUtfylt()).isTrue();
        assertThat(utdanningSporsmal.getFelt()).hasSize(1);
        assertThat(utdanningSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("dinsituasjon.studerer.true");
        assertThat(utdanningSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
        assertThat(utdanningSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);

        var studentgradSporsmal = res.getAvsnitt().get(1).getSporsmal().get(1);
        assertThat(studentgradSporsmal.getErUtfylt()).isFalse();
        assertThat(studentgradSporsmal.getFelt()).isNull();
    }

    @Test
    void harUtfyltStudentgrad() {
        var soknad = createSoknad(new JsonArbeid(), heltidstudent);
        var res = this.steg.get(soknad);

        assertThat(res.getAvsnitt().get(1).getSporsmal()).hasSize(2);

        var utdanningSporsmal = res.getAvsnitt().get(1).getSporsmal().get(0);
        assertThat(utdanningSporsmal.getErUtfylt()).isTrue();
        assertThat(utdanningSporsmal.getFelt()).hasSize(1);
        assertThat(utdanningSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("dinsituasjon.studerer.true");
        assertThat(utdanningSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
        assertThat(utdanningSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);

        var studentgradSporsmal = res.getAvsnitt().get(1).getSporsmal().get(1);
        assertThat(studentgradSporsmal.getErUtfylt()).isTrue();
        assertThat(studentgradSporsmal.getFelt()).hasSize(1);
        assertThat(studentgradSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("dinsituasjon.studerer.true.grad.heltid");
        assertThat(studentgradSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
        assertThat(studentgradSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
    }

    private JsonInternalSoknad createSoknad(JsonArbeid arbeid, JsonUtdanning utdanning) {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withArbeid(arbeid)
                                .withUtdanning(utdanning)
                        )
                );
    }
}