package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.familie.SivilstatusRessurs.EktefelleFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.familie.SivilstatusRessurs.SivilstatusFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
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

import java.text.ParseException;
import java.util.Optional;

import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.getPersonnummerFromFnr;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SivilstatusRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final JsonEktefelle JSON_EKTEFELLE = new JsonEktefelle()
            .withNavn(new JsonNavn()
                    .withFornavn("Alfred")
                    .withMellomnavn("Thaddeus Crane")
                    .withEtternavn("Pennyworth"))
            .withFodselsdato("1940-01-01")
            .withPersonIdentifikator("01014012345");

    private static final EktefelleFrontend EKTEFELLE_FRONTEND = new EktefelleFrontend()
            .withNavn(new NavnFrontend("Alfred", "Thaddeus Crane", "Pennyworth"))
            .withFodselsdato("1940-01-01")
            .withPersonnummer("12345");

    @Mock
    private LegacyHelper legacyHelper;

    @InjectMocks
    private SivilstatusRessurs sivilstatusRessurs;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

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
    public void getSivilstatusSkalReturnereNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(null, null, null,
                        null, null, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend, nullValue());
    }

    @Test
    public void getSivilstatusSkalReturnereKunBrukerdefinertStatus(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(true, JsonSivilstatus.Status.GIFT, null,
                        null, null, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.sivilstatus, is(JsonSivilstatus.Status.GIFT));
        assertThat(sivilstatusFrontend.kildeErSystem, is(false));
        assertThat(sivilstatusFrontend.ektefelle, nullValue());
        assertThat(sivilstatusFrontend.harDiskresjonskode, nullValue());
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen, nullValue());
    }

    @Test
    public void getSivilstatusSkalReturnereBrukerdefinertEktefelleRiktigKonvertert(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(true, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE,
                        null, null, true));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.kildeErSystem, is(false));
        assertThat(sivilstatusFrontend.sivilstatus, is(JsonSivilstatus.Status.GIFT));
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend.ektefelle, JSON_EKTEFELLE);
        assertThat(sivilstatusFrontend.harDiskresjonskode, nullValue());
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen, nullValue());
        assertThat(sivilstatusFrontend.borSammenMed, is(true));
    }

    @Test
    public void getSivilstatusSkalReturnereSystemdefinertEktefelleRiktigKonvertert(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(false, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE,
                        false, true, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.kildeErSystem, is(true));
        assertThat(sivilstatusFrontend.sivilstatus, is(JsonSivilstatus.Status.GIFT));
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend.ektefelle, JSON_EKTEFELLE);
        assertThat(sivilstatusFrontend.harDiskresjonskode, is(false));
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen, is(true));
        assertThat(sivilstatusFrontend.borSammenMed, nullValue());
    }

    @Test
    public void getSivilstatusSkalReturnereSystemdefinertEktefelleMedDiskresjonskode(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithSivilstatus(false, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE,
                        true, null, null));

        final SivilstatusFrontend sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID);

        assertThat(sivilstatusFrontend.kildeErSystem, is(true));
        assertThat(sivilstatusFrontend.sivilstatus, is(JsonSivilstatus.Status.GIFT));
        assertThat(sivilstatusFrontend.harDiskresjonskode, is(true));
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen, nullValue());
        assertThat(sivilstatusFrontend.borSammenMed, nullValue());
    }

    @Test
    public void putSivilstatusSkalKunneSetteAlleTyperSivilstatus() throws ParseException {
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithSivilstatus(null, null, null,
                        null, null, null)));

        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.GIFT);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.ENKE);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SAMBOER);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SEPARERT);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SKILT);
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.UGIFT);
    }

    @Test
    public void putSivilstatusSkalSetteStatusGiftOgEktefelle() throws ParseException {
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithSivilstatus(null, null, null,
                        null, null, null)));

        final SivilstatusFrontend sivilstatusFrontend = new SivilstatusFrontend()
                .withKildeErSystem(false).withSivilstatus(JsonSivilstatus.Status.GIFT)
                .withEktefelle(EKTEFELLE_FRONTEND);

        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();
        assertThat(sivilstatus.getKilde(), is(JsonKilde.BRUKER));
        assertThat(sivilstatus.getStatus(), is(JsonSivilstatus.Status.GIFT));
        assertThatEktefelleIsCorrectlyConverted(EKTEFELLE_FRONTEND, sivilstatus.getEktefelle());
    }

    private void assertThatEktefelleIsCorrectlyConverted(EktefelleFrontend ektefelle, JsonEktefelle jsonEktefelle) {
        assertThat("fodselsdato", ektefelle.fodselsdato, is(jsonEktefelle.getFodselsdato()));
        assertThat("personIdentifikator", ektefelle.personnummer, is(getPersonnummerFromFnr(jsonEktefelle.getPersonIdentifikator())));
        assertThat("fornavn", ektefelle.navn.fornavn, is(jsonEktefelle.getNavn().getFornavn()));
        assertThat("mellomnavn", ektefelle.navn.mellomnavn, is(jsonEktefelle.getNavn().getMellomnavn()));
        assertThat("etternavn", ektefelle.navn.etternavn, is(jsonEktefelle.getNavn().getEtternavn()));
    }

    private void assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status status) throws ParseException {
        final SivilstatusFrontend sivilstatusFrontend = new SivilstatusFrontend()
                .withKildeErSystem(false).withSivilstatus(status);

        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonSivilstatus sivilstatus = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().getSivilstatus();
        assertThat("kilde", sivilstatus.getKilde(), is(JsonKilde.BRUKER));
        assertThat("status", sivilstatus.getStatus(), is(status));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository, atLeastOnce()).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private void ignoreTilgangskontrollAndLegacyUpdate() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        when(faktaService.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum());
        when(faktaService.lagreBrukerFaktum(any(Faktum.class))).thenReturn(new Faktum());
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithSivilstatus(Boolean brukerutfylt, JsonSivilstatus.Status status,
                                                                      JsonEktefelle ektefelle, Boolean harDiskresjonskode,
                                                                      Boolean folkeregistrertMed, Boolean borSammen) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withFamilie(new JsonFamilie()
                                                .withSivilstatus(brukerutfylt == null ? null :
                                                        new JsonSivilstatus()
                                                                .withKilde(brukerutfylt ? JsonKilde.BRUKER : JsonKilde.SYSTEM)
                                                                .withStatus(status)
                                                                .withEktefelle(ektefelle)
                                                                .withEktefelleHarDiskresjonskode(harDiskresjonskode)
                                                                .withFolkeregistrertMedEktefelle(folkeregistrertMed)
                                                                .withBorSammenMed(borSammen)
                                                )
                                        )
                                )
                        )
                );
    }

}
