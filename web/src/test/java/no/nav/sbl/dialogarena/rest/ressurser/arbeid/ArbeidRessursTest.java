package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.arbeid.ArbeidRessurs.ArbeidFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.arbeid.ArbeidRessurs.ArbeidsforholdFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.ArbeidsforholdSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String KOMMENTAR = "Hath not the potter power over the clay, to make one vessel unto honor and another unto dishonor?";

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

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @InjectMocks
    private ArbeidRessurs arbeidRessurs;


    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getArbeidSkalReturnereSystemArbeidsforholdRiktigKonvertert(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithArbeid(createArbeidsforholdListe(), null));
        when(arbeidsforholdSystemdata.innhentSystemArbeidsforhold(anyString())).thenReturn(createArbeidsforholdListe());

        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);
        final List<ArbeidsforholdFrontend> arbeidsforholdFrontends = arbeidFrontend.arbeidsforhold;

        assertThat(arbeidsforholdFrontends.size(), is(2));
        final ArbeidsforholdFrontend arbeidsforhold_1 = arbeidsforholdFrontends.get(0);
        final ArbeidsforholdFrontend arbeidsforhold_2 = arbeidsforholdFrontends.get(1);

        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_1, ARBEIDSFORHOLD_1);
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_2, ARBEIDSFORHOLD_2);
    }

    @Test
    public void getArbeidSkalReturnereArbeidsforholdLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithArbeid(null, null));
        when(arbeidsforholdSystemdata.innhentSystemArbeidsforhold(anyString())).thenReturn(null);

        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);

        assertThat(arbeidFrontend.arbeidsforhold, nullValue());
    }

    @Test
    public void getArbeidSkalReturnereKommentarTilArbeidsforholdLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithArbeid(null, null));

        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);

        assertThat(arbeidFrontend.kommentarTilArbeidsforhold, nullValue());
    }

    @Test
    public void getArbeidSkalReturnereKommentarTilArbeidsforhold(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithArbeid(null, KOMMENTAR));

        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);

        assertThat(arbeidFrontend.kommentarTilArbeidsforhold, is(KOMMENTAR));
    }

    @Test
    public void putArbeidSkalLageNyJsonKommentarTilArbeidsforholdDersomDenVarNull(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithArbeid(null, null)));

        final ArbeidFrontend arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold(KOMMENTAR);
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
        assertThat(kommentarTilArbeidsforhold.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(kommentarTilArbeidsforhold.getVerdi(), is(KOMMENTAR));
    }

    @Test
    public void putArbeidSkalOppdatereKommentarTilArbeidsforhold(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar")));

        final ArbeidFrontend arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold(KOMMENTAR);
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
        assertThat(kommentarTilArbeidsforhold.getKilde(), is(JsonKildeBruker.BRUKER));
        assertThat(kommentarTilArbeidsforhold.getVerdi(), is(KOMMENTAR));
    }

    @Test
    public void putArbeidSkalSetteLikNullDersomKommentarenErTom(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar")));

        final ArbeidFrontend arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold("");
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
        assertThat(kommentarTilArbeidsforhold, nullValue());
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private void ignoreTilgangskontrollAndLegacyUpdate() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        when(faktaService.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum());
        when(faktaService.lagreBrukerFaktum(any(Faktum.class))).thenReturn(new Faktum());
    }

    private void assertThatArbeidsforholdIsCorrectlyConverted(ArbeidsforholdFrontend forholdFrontend, JsonArbeidsforhold jsonForhold) {
        assertThat("arbeidsgivernavn", forholdFrontend.arbeidsgivernavn, is(jsonForhold.getArbeidsgivernavn()));
        assertThat("fom", forholdFrontend.fom, is(jsonForhold.getFom()));
        assertThat("tom", forholdFrontend.tom, is(jsonForhold.getTom()));
        assertThat("stillingsprosent", forholdFrontend.stillingsprosent, is(jsonForhold.getStillingsprosent()));
        assertThatStillingstypeIsCorrect(forholdFrontend.stillingstypeErHeltid, jsonForhold.getStillingstype());
        assertThat("overstyrtAvBruker", forholdFrontend.overstyrtAvBruker, is(Boolean.FALSE));
    }

    private void assertThatStillingstypeIsCorrect(Boolean stillingstypeErHeltid, JsonArbeidsforhold.Stillingstype stillingstype){
        if (stillingstypeErHeltid == null){
            return;
        }
        if (stillingstypeErHeltid){
            assertThat("stillingstype", stillingstype, is(JsonArbeidsforhold.Stillingstype.FAST));
        } else {
            assertThat("stillingstype", stillingstype, is(JsonArbeidsforhold.Stillingstype.VARIABEL));
        }
    }

    private List<JsonArbeidsforhold> createArbeidsforholdListe(){
        List<JsonArbeidsforhold> forholdListe = new ArrayList<>();
        forholdListe.add(ARBEIDSFORHOLD_1);
        forholdListe.add(ARBEIDSFORHOLD_2);
        return forholdListe;
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithArbeid(List<JsonArbeidsforhold> arbeidsforholdList, String kommentar) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withArbeid(new JsonArbeid()
                                                .withForhold(arbeidsforholdList)
                                                .withKommentarTilArbeidsforhold(kommentar == null ? null : new JsonKommentarTilArbeidsforhold()
                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                        .withVerdi(kommentar)
                                                )
                                        )
                                )
                        )
                );
    }
}
