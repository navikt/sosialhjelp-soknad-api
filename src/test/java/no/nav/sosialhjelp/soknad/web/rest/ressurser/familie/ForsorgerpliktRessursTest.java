package no.nav.sosialhjelp.soknad.web.rest.ressurser.familie;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.NavnFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.familie.ForsorgerpliktRessurs.AnsvarFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.familie.ForsorgerpliktRessurs.BarnFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.familie.ForsorgerpliktRessurs.ForsorgerpliktFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.web.rest.mappers.PersonMapper.getPersonnummerFromFnr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForsorgerpliktRessursTest {

    private static final String EIER = "123456789101";
    private static final String BEHANDLINGSID = "123";
    private static final JsonBarn JSON_BARN = new JsonBarn()
            .withKilde(JsonKilde.SYSTEM)
            .withNavn(new JsonNavn()
                    .withFornavn("Amadeus")
                    .withMellomnavn("Wolfgang")
                    .withEtternavn("Mozart"))
            .withFodselsdato("1756-01-27")
            .withPersonIdentifikator("11111111111");

    private static final JsonBarn JSON_BARN_2 = new JsonBarn()
            .withKilde(JsonKilde.SYSTEM)
            .withNavn(new JsonNavn()
                    .withFornavn("Ludwig")
                    .withMellomnavn("van")
                    .withEtternavn("Beethoven"))
            .withFodselsdato("1770-12-16")
            .withPersonIdentifikator("22222222222");

    private static final AnsvarFrontend BRUKERREGISTRERT_BARN = new AnsvarFrontend()
            .withBarn(new BarnFrontend()
                    .withNavn(new NavnFrontend("Harry", "Trollmann", "Potter"))
                    .withFodselsdato("1991-01-01"))
            .withBorSammenMed(true)
            .withHarDeltBosted(true);

    @InjectMocks
    private ForsorgerpliktRessurs forsorgerpliktRessurs;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void getForsorgerpliktSkalReturnereTomForsorgerplikt(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(null, null, null));

        ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isNull();
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull();
        assertThat(forsorgerpliktFrontend.ansvar).isNull();
    }

    @Test
    void getForsorgerpliktSkalReturnereEtBarnSomErFolkeregistrertSammenOgHarDeltBosted(){
        JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN)
                .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(true))
                .withHarDeltBosted(new JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(true));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(true, null, Collections.singletonList(jsonAnsvar)));

        ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue();
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull();
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(1);
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
    }

    @Test
    void getForsorgerpliktSkalReturnereEtBarnSomIkkeErFolkeregistrertSammenMenHarSamvarsgrad(){
        JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN)
                .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(false))
                .withSamvarsgrad(new JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(30));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(true, null, Collections.singletonList(jsonAnsvar)));

        ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue();
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull();
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(1);
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
    }

    @Test
    void getForsorgerpliktSkalReturnereToBarn(){
        JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        JsonAnsvar jsonAnsvar_2 = new JsonAnsvar().withBarn(JSON_BARN_2);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(true, null, asList(jsonAnsvar, jsonAnsvar_2)));

        ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue();
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull();
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(2);
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(1), jsonAnsvar_2);
    }

    @Test
    void getForsorgerpliktSkalReturnereEtBarnOgBarnebidrag(){
        JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(true, JsonBarnebidrag.Verdi.BEGGE, Collections.singletonList(jsonAnsvar)));

        ForsorgerpliktFrontend forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID);

        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue();
        assertThat(forsorgerpliktFrontend.barnebidrag).isEqualTo(JsonBarnebidrag.Verdi.BEGGE);
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(1);
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar.get(0), jsonAnsvar);
    }

    @Test
    void putForsorgerpliktSkalSetteBarnebidrag(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(null, null, null));
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");

        ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withBarnebidrag(JsonBarnebidrag.Verdi.BETALER);

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getBarnebidrag().getVerdi()).isEqualTo(JsonBarnebidrag.Verdi.BETALER);
        assertThat(forsorgerplikt.getHarForsorgerplikt()).isNull();
        assertThat(forsorgerplikt.getAnsvar()).isNull();
    }

    @Test
    void putForsorgerpliktSkalFjerneBarnebidragOgInntektOgUtgiftKnyttetTilBarnebidrag(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithForsorgerplikt(null, JsonBarnebidrag.Verdi.BEGGE, null);
        List<JsonOkonomioversiktInntekt> inntekt = new ArrayList<>();
        inntekt.add(new JsonOkonomioversiktInntekt().withType("barnebidrag"));
        List<JsonOkonomioversiktUtgift> utgift = new ArrayList<>();
        utgift.add(new JsonOkonomioversiktUtgift().withType("barnebidrag"));
        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setInntekt(inntekt);
        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setUtgift(utgift);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withBarnebidrag(null);

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        List<JsonOkonomioversiktInntekt> inntekter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();
        List<JsonOkonomioversiktUtgift> utgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getUtgift();
        assertThat(forsorgerplikt.getBarnebidrag()).isNull();
        assertThat(inntekter.isEmpty()).isTrue();
        assertThat(utgifter.isEmpty()).isTrue();
    }

    @Test
    void putForsorgerpliktSkalSetteHarDeltBostedOgSamvarsgradPaaToBarn(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        JsonAnsvar jsonAnsvar_2 = new JsonAnsvar().withBarn(JSON_BARN_2);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(true, null, asList(jsonAnsvar, jsonAnsvar_2)));

        ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withAnsvar(asList(createBarnMedDeltBosted(), createBarnMedSamvarsgrad()));

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getBarnebidrag()).isNull();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        assertThat(forsorgerplikt.getAnsvar().get(0).getHarDeltBosted().getVerdi()).isTrue();
        assertThat(forsorgerplikt.getAnsvar().get(1).getSamvarsgrad().getVerdi()).isEqualTo(30);
    }

    @Test
    void putForsorgerpliktSkalLeggeTilBrukerregistrertBarnVedSidenAvSystemregistrerte(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        JsonAnsvar jsonAnsvar = new JsonAnsvar().withBarn(JSON_BARN);
        JsonAnsvar jsonAnsvar_2 = new JsonAnsvar().withBarn(JSON_BARN_2);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(true, null, asList(jsonAnsvar, jsonAnsvar_2)));

        ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withAnsvar(asList(createBarnMedDeltBosted(), createBarnMedSamvarsgrad()))
                .withBrukerregistrertAnsvar(asList(BRUKERREGISTRERT_BARN));

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getBarnebidrag()).isNull();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        assertThat(forsorgerplikt.getAnsvar().get(0).getHarDeltBosted().getVerdi()).isTrue();
        assertThat(forsorgerplikt.getAnsvar().get(1).getSamvarsgrad().getVerdi()).isEqualTo(30);
        JsonAnsvar brukerregistrertAnsvar = forsorgerplikt.getAnsvar().get(2);
        assertThatAnsvarIsCorrectlyConverted(BRUKERREGISTRERT_BARN, brukerregistrertAnsvar);
    }

    @Test
    void putForsorgerpliktSkalLeggeTilBrukerregistrertBarnOgSetteHarForsorgerplikt(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithForsorgerplikt(false, null, new ArrayList<>()));

        ForsorgerpliktFrontend forsorgerpliktFrontend = new ForsorgerpliktFrontend()
                .withBrukerregistrertAnsvar(asList(BRUKERREGISTRERT_BARN));

        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonForsorgerplikt forsorgerplikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();
        assertThat(forsorgerplikt.getBarnebidrag()).isNull();
        assertThat(forsorgerplikt.getHarForsorgerplikt().getVerdi()).isTrue();
        JsonAnsvar brukerregistrertAnsvar = forsorgerplikt.getAnsvar().get(0);
        assertThatAnsvarIsCorrectlyConverted(BRUKERREGISTRERT_BARN, brukerregistrertAnsvar);
    }

    @Test
    void getForsorgerpliktSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void putForsorgerpliktSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var forsorgerpliktFrontend = new ForsorgerpliktFrontend().withBrukerregistrertAnsvar(asList(BRUKERREGISTRERT_BARN));

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend));

        verifyNoInteractions(soknadUnderArbeidRepository);
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
        BarnFrontend barnFrontend = ansvarFrontend.barn;
        JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat(ansvarFrontend.borSammenMed).isEqualTo(jsonAnsvar.getBorSammenMed() == null ? null : jsonAnsvar.getBorSammenMed().getVerdi());
        assertThat(ansvarFrontend.harDeltBosted).isEqualTo(jsonAnsvar.getHarDeltBosted() == null ? null : jsonAnsvar.getHarDeltBosted().getVerdi());
        assertThat(ansvarFrontend.samvarsgrad).isEqualTo(jsonAnsvar.getSamvarsgrad() == null ? null : jsonAnsvar.getSamvarsgrad().getVerdi());
        assertThat(ansvarFrontend.erFolkeregistrertSammen).isEqualTo(jsonAnsvar.getErFolkeregistrertSammen() == null ? null : jsonAnsvar.getErFolkeregistrertSammen().getVerdi());
        assertThat(barnFrontend.fodselsnummer).isEqualTo(jsonBarn.getPersonIdentifikator());
        assertThat(barnFrontend.fodselsdato).isEqualTo(jsonBarn.getFodselsdato());
        assertThat(barnFrontend.navn.fornavn).isEqualTo(jsonBarn.getNavn().getFornavn());
        assertThat(barnFrontend.navn.mellomnavn).isEqualTo(jsonBarn.getNavn().getMellomnavn());
        assertThat(barnFrontend.navn.etternavn).isEqualTo(jsonBarn.getNavn().getEtternavn());
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
