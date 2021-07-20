package no.nav.sosialhjelp.soknad.web.rest.ressurser.familie;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.NavnFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.familie.SivilstatusRessurs.EktefelleFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.familie.SivilstatusRessurs.SivilstatusFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;

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
class SivilstatusRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final JsonEktefelle JSON_EKTEFELLE = new JsonEktefelle()
            .withNavn(new JsonNavn()
                    .withFornavn("Alfred")
                    .withMellomnavn("Thaddeus Crane")
                    .withEtternavn("Pennyworth"))
            .withFodselsdato("1940-01-01")
            .withPersonIdentifikator("11111111111");

    private static final EktefelleFrontend EKTEFELLE_FRONTEND = new EktefelleFrontend()
            .withNavn(new NavnFrontend("Alfred", "Thaddeus Crane", "Pennyworth"))
            .withFodselsdato("1940-01-01")
            .withPersonnummer("12345");

    @InjectMocks
    private SivilstatusRessurs sivilstatusRessurs;

    @Mock
    private Tilgangskontroll tilgangskontroll;

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
    void getSivilstatusSkalReturnereNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(null, null, null,
                        null, null, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend).isNull();
    }

    @Test
    void getSivilstatusSkalReturnereKunBrukerdefinertStatus(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(true, JsonSivilstatus.Status.GIFT, null,
                        null, null, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT);
        assertThat(sivilstatusFrontend.kildeErSystem).isFalse();
        assertThat(sivilstatusFrontend.ektefelle).isNull();
        assertThat(sivilstatusFrontend.harDiskresjonskode).isNull();
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen).isNull();
    }

    @Test
    void getSivilstatusSkalReturnereBrukerdefinertEktefelleRiktigKonvertert(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(true, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE,
                        null, null, true));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.kildeErSystem).isFalse();
        assertThat(sivilstatusFrontend.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT);
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend.ektefelle, JSON_EKTEFELLE);
        assertThat(sivilstatusFrontend.harDiskresjonskode).isNull();
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen).isNull();
        assertThat(sivilstatusFrontend.borSammenMed).isTrue();
    }

    @Test
    void getSivilstatusSkalReturnereSystemdefinertEktefelleRiktigKonvertert(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(false, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE,
                        false, true, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.kildeErSystem).isTrue();
        assertThat(sivilstatusFrontend.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT);
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend.ektefelle, JSON_EKTEFELLE);
        assertThat(sivilstatusFrontend.harDiskresjonskode).isFalse();
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen).isTrue();
        assertThat(sivilstatusFrontend.borSammenMed).isNull();
    }

    @Test
    void getSivilstatusSkalReturnereSystemdefinertEktefelleMedDiskresjonskode(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(false, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE,
                        true, null, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.kildeErSystem).isTrue();
        assertThat(sivilstatusFrontend.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT);
        assertThat(sivilstatusFrontend.harDiskresjonskode).isTrue();
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen).isNull();
        assertThat(sivilstatusFrontend.borSammenMed).isNull();
    }

    @Test
    void putSivilstatusSkalKunneSetteAlleTyperSivilstatus() throws ParseException {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(null, null, null,
                        null, null, null));

        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.GIFT);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.ENKE);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SAMBOER);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SEPARERT);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SKILT);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.UGIFT);
    }

    @Test
    void putSivilstatusSkalSetteStatusGiftOgEktefelle() throws ParseException {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(null, null, null,
                        null, null, null));

        final SivilstatusFrontend sivilstatusFrontend = new SivilstatusFrontend()
                .withKildeErSystem(false).withSivilstatus(JsonSivilstatus.Status.GIFT)
                .withEktefelle(EKTEFELLE_FRONTEND);

        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();
        assertThat(sivilstatus.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(sivilstatus.getStatus()).isEqualTo(JsonSivilstatus.Status.GIFT);
        assertThatEktefelleIsCorrectlyConverted(EKTEFELLE_FRONTEND, sivilstatus.getEktefelle());
    }

    @Test
    void getSivilstatusSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void putSivilstatusSkalKasteAuthorizationExceptionVedManglendeTilgang() throws ParseException {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var sivilstatusFrontend = new SivilstatusFrontend()
                .withKildeErSystem(false).withSivilstatus(JsonSivilstatus.Status.GIFT)
                .withEktefelle(EKTEFELLE_FRONTEND);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private void assertThatEktefelleIsCorrectlyConverted(EktefelleFrontend ektefelle, JsonEktefelle jsonEktefelle) {
        assertThat(ektefelle.fodselsdato).isEqualTo(jsonEktefelle.getFodselsdato());
        assertThat(ektefelle.personnummer).isEqualTo(getPersonnummerFromFnr(jsonEktefelle.getPersonIdentifikator()));
        assertThat(ektefelle.navn.fornavn).isEqualTo(jsonEktefelle.getNavn().getFornavn());
        assertThat(ektefelle.navn.mellomnavn).isEqualTo(jsonEktefelle.getNavn().getMellomnavn());
        assertThat(ektefelle.navn.etternavn).isEqualTo(jsonEktefelle.getNavn().getEtternavn());
    }

    private void assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status status) throws ParseException {
        final SivilstatusFrontend sivilstatusFrontend = new SivilstatusFrontend()
                .withKildeErSystem(false).withSivilstatus(status);

        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();
        assertThat(sivilstatus.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(sivilstatus.getStatus()).isEqualTo(status);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository, atLeastOnce()).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithSivilstatus(Boolean brukerutfylt, JsonSivilstatus.Status status,
                                                                      JsonEktefelle ektefelle, Boolean harDiskresjonskode,
                                                                      Boolean folkeregistrertMed, Boolean borSammen) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie()
                .withSivilstatus(brukerutfylt == null ? null :
                        new JsonSivilstatus()
                                .withKilde(brukerutfylt ? JsonKilde.BRUKER : JsonKilde.SYSTEM)
                                .withStatus(status)
                                .withEktefelle(ektefelle)
                                .withEktefelleHarDiskresjonskode(harDiskresjonskode)
                                .withFolkeregistrertMedEktefelle(folkeregistrertMed)
                                .withBorSammenMed(borSammen));
        return soknadUnderArbeid;
    }

}
