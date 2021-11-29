package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ArbeidOgUtdanningStegTest {

    private val steg = ArbeidOgUtdanningSteg()

    private val arbeidsforholdMedSlutt = JsonArbeidsforhold()
        .withArbeidsgivernavn("arbeidsgiver")
        .withFom("01.01.2021")
        .withTom("10.10.2021")
        .withStillingsprosent(100)
    private val arbeidsforholdUtenSlutt = JsonArbeidsforhold()
        .withArbeidsgivernavn("arbeidsgiver2")
        .withFom("01.01.2021")
        .withStillingsprosent(100)
    private val ikkeStudent = JsonUtdanning().withErStudent(false)
    private val studentUtenStudentgrad = JsonUtdanning().withErStudent(true)
    private val heltidstudent = JsonUtdanning().withErStudent(true).withStudentgrad(JsonUtdanning.Studentgrad.HELTID)

    @Test
    fun ingenArbeidsforhold() {
        val soknad = createSoknad(JsonArbeid(), JsonUtdanning())
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val arbeidsforholdSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(arbeidsforholdSporsmal.tittel).isEqualTo("arbeidsforhold.ingen")
        assertThat(arbeidsforholdSporsmal.erUtfylt).isTrue
        assertThat(arbeidsforholdSporsmal.felt).isNull()
    }

    @Test
    fun arbeidsforholdMedSlutt() {
        val soknad = createSoknad(JsonArbeid().withForhold(listOf(arbeidsforholdMedSlutt)), JsonUtdanning())
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val arbeidsforholdSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(arbeidsforholdSporsmal.tittel).isEqualTo("arbeidsforhold.infotekst")
        assertThat(arbeidsforholdSporsmal.erUtfylt).isTrue
        assertThat(arbeidsforholdSporsmal.felt).hasSize(1)

        val felt = arbeidsforholdSporsmal.felt!![0]
        assertThat(felt.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(felt.labelSvarMap).hasSize(4)
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.arbeidsgivernavn.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.arbeidsgivernavn.label"]!!.value).isEqualTo(arbeidsforholdMedSlutt.arbeidsgivernavn)
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.fom.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.fom.label"]!!.value).isEqualTo(arbeidsforholdMedSlutt.fom)
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.tom.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.tom.label"]!!.value).isEqualTo(arbeidsforholdMedSlutt.tom)
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.stillingsprosent.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.stillingsprosent.label"]!!.value).isEqualTo(arbeidsforholdMedSlutt.stillingsprosent.toString())
    }

    @Test
    fun arbeidsforholdUtenSlutt() {
        val soknad = createSoknad(JsonArbeid().withForhold(listOf(arbeidsforholdUtenSlutt)), JsonUtdanning())
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val arbeidsforholdSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(arbeidsforholdSporsmal.tittel).isEqualTo("arbeidsforhold.infotekst")
        assertThat(arbeidsforholdSporsmal.erUtfylt).isTrue
        assertThat(arbeidsforholdSporsmal.felt).hasSize(1)

        val felt = arbeidsforholdSporsmal.felt!![0]
        assertThat(felt.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(felt.labelSvarMap).hasSize(3)
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.arbeidsgivernavn.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.arbeidsgivernavn.label"]!!.value).isEqualTo(arbeidsforholdUtenSlutt.arbeidsgivernavn)
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.fom.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.fom.label"]!!.value).isEqualTo(arbeidsforholdUtenSlutt.fom)
        assertThat(felt.labelSvarMap).doesNotContainKey("arbeidsforhold.tom.label")
        assertThat(felt.labelSvarMap).containsKey("arbeidsforhold.stillingsprosent.label")
        assertThat(felt.labelSvarMap!!["arbeidsforhold.stillingsprosent.label"]!!.value).isEqualTo(arbeidsforholdUtenSlutt.stillingsprosent.toString())
    }

    @Test
    fun arbeidsforholdMedKommentar() {
        val soknad = createSoknad(
            JsonArbeid()
                .withForhold(listOf(arbeidsforholdUtenSlutt))
                .withKommentarTilArbeidsforhold(JsonKommentarTilArbeidsforhold().withVerdi("kommentar")),
            JsonUtdanning()
        )
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(2)

        val arbeidsforholdSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(arbeidsforholdSporsmal.tittel).isEqualTo("arbeidsforhold.infotekst")
        assertThat(arbeidsforholdSporsmal.erUtfylt).isTrue

        val arbeidsforholdKommentarSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(arbeidsforholdKommentarSporsmal.tittel).isEqualTo("opplysninger.arbeidsituasjon.kommentarer.label")
        assertThat(arbeidsforholdKommentarSporsmal.erUtfylt).isTrue
        assertThat(arbeidsforholdKommentarSporsmal.felt).hasSize(1)
        validateFeltMedSvar(arbeidsforholdKommentarSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "kommentar")
    }

    @Test
    fun ikkeUtfyltStudent() {
        val soknad = createSoknad(JsonArbeid(), JsonUtdanning())
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[1].sporsmal).hasSize(1)

        val utdanningSporsmal = res.avsnitt[1].sporsmal[0]
        assertThat(utdanningSporsmal.tittel).isEqualTo("dinsituasjon.studerer.sporsmal")
        assertThat(utdanningSporsmal.erUtfylt).isFalse
        assertThat(utdanningSporsmal.felt).isNull()
    }

    @Test
    fun erIkkeStudent() {
        val soknad = createSoknad(JsonArbeid(), ikkeStudent)
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[1].sporsmal).hasSize(1)

        val utdanningSporsmal = res.avsnitt[1].sporsmal[0]
        assertThat(utdanningSporsmal.erUtfylt).isTrue
        assertThat(utdanningSporsmal.felt).hasSize(1)
        validateFeltMedSvar(utdanningSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "dinsituasjon.studerer.false")
    }

    @Test
    fun ikkeUtfyltStudentgrad() {
        val soknad = createSoknad(JsonArbeid(), studentUtenStudentgrad)
        val res = steg.get(soknad)
        assertThat(res.avsnitt[1].sporsmal).hasSize(2)

        val utdanningSporsmal = res.avsnitt[1].sporsmal[0]
        assertThat(utdanningSporsmal.erUtfylt).isTrue
        assertThat(utdanningSporsmal.felt).hasSize(1)
        validateFeltMedSvar(utdanningSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "dinsituasjon.studerer.true")

        val studentgradSporsmal = res.avsnitt[1].sporsmal[1]
        assertThat(studentgradSporsmal.erUtfylt).isFalse
        assertThat(studentgradSporsmal.felt).isNull()
    }

    @Test
    fun harUtfyltStudentgrad() {
        val soknad = createSoknad(JsonArbeid(), heltidstudent)
        val res = steg.get(soknad)
        assertThat(res.avsnitt[1].sporsmal).hasSize(2)

        val utdanningSporsmal = res.avsnitt[1].sporsmal[0]
        assertThat(utdanningSporsmal.erUtfylt).isTrue
        assertThat(utdanningSporsmal.felt).hasSize(1)
        validateFeltMedSvar(utdanningSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "dinsituasjon.studerer.true")

        val studentgradSporsmal = res.avsnitt[1].sporsmal[1]
        assertThat(studentgradSporsmal.erUtfylt).isTrue
        assertThat(studentgradSporsmal.felt).hasSize(1)
        validateFeltMedSvar(studentgradSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "dinsituasjon.studerer.true.grad.heltid")
    }

    private fun createSoknad(arbeid: JsonArbeid, utdanning: JsonUtdanning): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withArbeid(arbeid)
                            .withUtdanning(utdanning)
                    )
            )
    }
}