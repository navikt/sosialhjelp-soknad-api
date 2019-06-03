package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.sbl.dialogarena.rest.ressurser.familie.ForsorgerpliktRessurs.AnsvarFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.familie.ForsorgerpliktRessurs.BarnFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.familie.ForsorgerpliktRessurs.ForsorgerpliktFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.getPersonnummerFromFnr;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ForsorgerpliktRessursTest {

    private static final String EIER = "123456789101";
    private static final String BEHANDLINGSID = "123";
    private static final JsonBarn JSON_BARN = new JsonBarn()
            .withKilde(JsonKilde.SYSTEM)
            .withNavn(new JsonNavn()
                    .withFornavn("Amadeus")
                    .withMellomnavn("Wolfgang")
                    .withEtternavn("Mozart"))
            .withFodselsdato("1756-01-27")
            .withPersonIdentifikator("27015612345");

    private static final JsonBarn JSON_BARN_2 = new JsonBarn()
            .withKilde(JsonKilde.SYSTEM)
            .withNavn(new JsonNavn()
                    .withFornavn("Ludwig")
                    .withMellomnavn("van")
                    .withEtternavn("Beethoven"))
            .withFodselsdato("1770-12-16")
            .withPersonIdentifikator("16127054321");

    @InjectMocks
    private ForsorgerpliktRessurs forsorgerpliktRessurs;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getForsorgerpliktSkalReturnereTomForsorgerplikt(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(
                createJsonInternalSoknadWithForsorgerplikt(null, null, null)));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt, nullValue());
        assertThat(forsorgerpliktFrontend.barnebidrag, nullValue());
        assertThat(forsorgerpliktFrontend.ansvar, nullValue());
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnSomErFolkeregistrertSammenOgHarDeltBosted(){
        final JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN)
                .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(true))
                .withHarDeltBosted(new JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(true));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(
                createJsonInternalSoknadWithForsorgerplikt(true, null, Collections.singletonList(jsonAnsvar))));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt, is(true));
        assertThat(forsorgerpliktFrontend.barnebidrag, nullValue());
        assertThat(forsorgerpliktFrontend.ansvar.size(), is(1));
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnSomIkkeErFolkeregistrertSammenMenHarSamvarsgrad(){
        final JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN)
                .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(false))
                .withSamvarsgrad(new JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(30));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(
                createJsonInternalSoknadWithForsorgerplikt(true, null, Collections.singletonList(jsonAnsvar))));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt, is(true));
        assertThat(forsorgerpliktFrontend.barnebidrag, nullValue());
        assertThat(forsorgerpliktFrontend.ansvar.size(), is(1));
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
    }

    @Test
    public void getForsorgerpliktSkalReturnereToBarn(){
        final JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        final JsonAnsvar jsonAnsvar_2 = new JsonAnsvar().withBarn(JSON_BARN_2);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(
                createJsonInternalSoknadWithForsorgerplikt(true, null, Arrays.asList(jsonAnsvar, jsonAnsvar_2))));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt, is(true));
        assertThat(forsorgerpliktFrontend.barnebidrag, nullValue());
        assertThat(forsorgerpliktFrontend.ansvar.size(), is(2));
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(1), jsonAnsvar_2);
    }

    @Test
    public void getForsorgerpliktSkalReturnereEtBarnOgBarnebidrag(){
        final JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(
                createJsonInternalSoknadWithForsorgerplikt(true, JsonBarnebidrag.Verdi.BEGGE, Collections.singletonList(jsonAnsvar))));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt, is(true));
        assertThat(forsorgerpliktFrontend.barnebidrag, is(JsonBarnebidrag.Verdi.BEGGE));
        assertThat(forsorgerpliktFrontend.ansvar.size(), is(1));
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
    }

    @Test
    public void putForsorgerpliktSkalSetteBarnebidrag(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithForsorgerplikt(null, null, null)));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withBarnebidrag(JsonBarnebidrag.Verdi.BETALER);

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getBarnebidrag().getVerdi(), is(JsonBarnebidrag.Verdi.BETALER));
        assertThat(forsorgerplikt.getHarForsorgerplikt(), nullValue());
        assertThat(forsorgerplikt.getAnsvar(), nullValue());
    }

    @Test
    public void putForsorgerpliktSkalSetteHarDeltBostedOgSamvarsgradPaaToBarn(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        final JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        final JsonAnsvar jsonAnsvar_2 = new JsonAnsvar().withBarn(JSON_BARN_2);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithForsorgerplikt(true, null, Arrays.asList(jsonAnsvar, jsonAnsvar_2))));

        final ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withAnsvar(Arrays.asList(createBarnMedDeltBosted(), createBarnMedSamvarsgrad()));

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getBarnebidrag(), nullValue());
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi(), is(true));
        assertThat(forsorgerplikt.getAnsvar().get(0).getHarDeltBosted().getVerdi(), is(true));
        assertThat(forsorgerplikt.getAnsvar().get(1).getSamvarsgrad().getVerdi(), is(30));
    }

    private AnsvarFrontend createBarnMedSamvarsgrad() {
        return new AnsvarFrontend()
                    .withBarn(new BarnFrontend()
                            .withFodselsnummer(JSON_BARN_2.getPersonIdentifikator())
                            .withPersonnummer(getPersonnummerFromFnr(JSON_BARN_2.getPersonIdentifikator())))
                    .withSamvarsgrad(30);
    }

    private AnsvarFrontend createBarnMedDeltBosted() {
        return new AnsvarFrontend()
                    .withBarn(new BarnFrontend()
                            .withFodselsnummer(JSON_BARN.getPersonIdentifikator())
                            .withPersonnummer(getPersonnummerFromFnr(JSON_BARN.getPersonIdentifikator())))
                    .withHarDeltBosted(true);
    }

    private void assertThatAnsvarIsCorrectlyConverted(AnsvarFrontend ansvarFrontend, JsonAnsvar jsonAnsvar) {
        final BarnFrontend barnFrontend = ansvarFrontend.barn;
        final JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat("harDiskresjonskode", ansvarFrontend.harDiskresjonskode, is(jsonBarn.getHarDiskresjonskode()));
        assertThat("borSammenMed", ansvarFrontend.borSammenMed, is(jsonAnsvar.getBorSammenMed()));
        assertThat("harDeltBosted", ansvarFrontend.harDeltBosted,
                is(jsonAnsvar.getHarDeltBosted() == null ? null : jsonAnsvar.getHarDeltBosted().getVerdi()));
        assertThat("samvarsgrad", ansvarFrontend.samvarsgrad,
                is(jsonAnsvar.getSamvarsgrad() == null ? null : jsonAnsvar.getSamvarsgrad().getVerdi()));
        assertThat("erFolkeregistrertSammen", ansvarFrontend.erFolkeregistrertSammen,
                is(jsonAnsvar.getErFolkeregistrertSammen() == null ? null : jsonAnsvar.getErFolkeregistrertSammen().getVerdi()));

        assertThat("fodselsnummer", barnFrontend.fodselsnummer, is(jsonBarn.getPersonIdentifikator()));
        assertThat("fodselsdato", barnFrontend.fodselsdato, is(jsonBarn.getFodselsdato()));
        assertThat("fornavn", barnFrontend.navn.fornavn, is(jsonBarn.getNavn().getFornavn()));
        assertThat("mellomnavn", barnFrontend.navn.mellomnavn, is(jsonBarn.getNavn().getMellomnavn()));
        assertThat("etternavn", barnFrontend.navn.etternavn, is(jsonBarn.getNavn().getEtternavn()));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository, atLeastOnce()).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithForsorgerplikt(Boolean harForsorgerplikt, JsonBarnebidrag.Verdi barnebidrag, List<JsonAnsvar> ansvars) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie()
                .withForsorgerplikt(new JsonForsorgerplikt()
                        .withHarForsorgerplikt(harForsorgerplikt == null ? null :
                                new JsonHarForsorgerplikt()
                                        .withKilde(JsonKilde.SYSTEM)
                                        .withVerdi(harForsorgerplikt))
                        .withBarnebidrag(barnebidrag == null ? null :
                                new JsonBarnebidrag()
                                        .withKilde(JsonKildeBruker.BRUKER)
                                        .withVerdi(barnebidrag))
                        .withAnsvar(ansvars));
        return soknadUnderArbeid;
    }

}
