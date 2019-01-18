package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.arbeid.ArbeidsforholdRessurs.ArbeidsforholdFrontend;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.ArbeidsforholdSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdRessursTest {

    private static final String BEHANDLINGSID = "123";

    private static final JsonArbeidsforhold ARBEIDSFORHOLD_1 = new JsonArbeidsforhold()
            .withKilde(JsonKilde.SYSTEM)
            .withArbeidsgivernavn("Good Corp.")
            .withFom("1337-01-01")
            .withTom("2020-01-01")
            .withStillingstype(JsonArbeidsforhold.Stillingstype.FAST)
            .withStillingsprosent(50)
            .withOverstyrtAvBruker(Boolean.FALSE);

    private static final JsonArbeidsforhold ARBEIDSFORHOLD_2 = new JsonArbeidsforhold()
                .withKilde(JsonKilde.SYSTEM)
                .withArbeidsgivernavn("Evil Corp.")
                .withFom("1337-02-02")
                .withTom("2020-02-02")
                .withStillingstype(JsonArbeidsforhold.Stillingstype.VARIABEL)
                .withStillingsprosent(30)
            .withOverstyrtAvBruker(Boolean.FALSE);

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private ArbeidsforholdSystemdata arbeidsforholdSystemdata;

    @InjectMocks
    private ArbeidsforholdRessurs arbeidsforholdRessurs;


    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
    }

    @Test
    public void getArbeidsforholdSkalReturnereSystemArbeidsforholdRiktigKonvertert(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithArbeidsforhold(createArbeidsforholdListe()));
        when(arbeidsforholdSystemdata.innhentSystemArbeidsforhold(anyString())).thenReturn(createArbeidsforholdListe());

        final List<ArbeidsforholdFrontend> arbeidsforholdFrontends = arbeidsforholdRessurs.hentArbeidsforhold(BEHANDLINGSID);

        assertThat(arbeidsforholdFrontends.size(), is(2));
        final ArbeidsforholdFrontend arbeidsforhold_1 = arbeidsforholdFrontends.get(0);
        final ArbeidsforholdFrontend arbeidsforhold_2 = arbeidsforholdFrontends.get(1);

        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_1, ARBEIDSFORHOLD_1);
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_2, ARBEIDSFORHOLD_2);
    }

    @Test
    public void getArbeidsforholdSkalReturnereOppdatertSystemArbeidsforholdFraTPS(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithoutArbeidsforhold());
        when(arbeidsforholdSystemdata.innhentSystemArbeidsforhold(anyString())).thenReturn(createArbeidsforholdListe());

        final List<ArbeidsforholdFrontend> arbeidsforholdFrontends = arbeidsforholdRessurs.hentArbeidsforhold(BEHANDLINGSID);

        assertThat(arbeidsforholdFrontends.size(), is(2));
        final ArbeidsforholdFrontend arbeidsforhold_1 = arbeidsforholdFrontends.get(0);
        final ArbeidsforholdFrontend arbeidsforhold_2 = arbeidsforholdFrontends.get(1);

        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_1, ARBEIDSFORHOLD_1);
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_2, ARBEIDSFORHOLD_2);
    }

    @Test
    public void getArbeidsforholdSkalReturnereArbeidsforholdLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithoutArbeidsforhold());
        when(arbeidsforholdSystemdata.innhentSystemArbeidsforhold(anyString())).thenReturn(null);

        final List<ArbeidsforholdFrontend> arbeidsforholdFrontends = arbeidsforholdRessurs.hentArbeidsforhold(BEHANDLINGSID);

        assertThat(arbeidsforholdFrontends, nullValue());
    }

    private void assertThatArbeidsforholdIsCorrectlyConverted(ArbeidsforholdFrontend forholdFrontend, JsonArbeidsforhold jsonForhold) {
        assertThat("arbeidsgivernavn feilet", forholdFrontend.arbeidsgivernavn, is(jsonForhold.getArbeidsgivernavn()));
        assertThat("fom feilet", forholdFrontend.fom, is(jsonForhold.getFom()));
        assertThat("tom feilet", forholdFrontend.tom, is(jsonForhold.getTom()));
        assertThat("stillingsprosent feilet", forholdFrontend.stillingsprosent, is(jsonForhold.getStillingsprosent()));
        assertThatStillingstypeIsCorrect(forholdFrontend.stillingstypeErHeltid, jsonForhold.getStillingstype());
        assertThat("overstyrtAvBruker feilet", forholdFrontend.overstyrtAvBruker, is(Boolean.FALSE));
    }

    private void assertThatStillingstypeIsCorrect(Boolean stillingstypeErHeltid, JsonArbeidsforhold.Stillingstype stillingstype){
        if (stillingstypeErHeltid == null){
            return;
        }
        if (stillingstypeErHeltid){
            assertThat("stillingstype feilet", stillingstype, is(JsonArbeidsforhold.Stillingstype.FAST));
        } else {
            assertThat("stillingstype feilet", stillingstype, is(JsonArbeidsforhold.Stillingstype.VARIABEL));
        }
    }

    private List<JsonArbeidsforhold> createArbeidsforholdListe(){
        List<JsonArbeidsforhold> forholdListe = new ArrayList<>();
        forholdListe.add(ARBEIDSFORHOLD_1);
        forholdListe.add(ARBEIDSFORHOLD_2);
        return forholdListe;
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithArbeidsforhold(List<JsonArbeidsforhold> arbeidsforholdList) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withArbeid(new JsonArbeid()
                                                .withForhold(arbeidsforholdList)
                                        )
                                )
                        )
                );
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithoutArbeidsforhold() {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withArbeid(new JsonArbeid())
                                )
                        )
                );
    }
}
