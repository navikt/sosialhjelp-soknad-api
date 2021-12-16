package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.OppgaveHandterer;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sosialhjelp.soknad.common.systemdata.SystemdataUpdater;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService;
import no.nav.sosialhjelp.soknad.innsending.InnsendingService;
import no.nav.sosialhjelp.soknad.tekster.TextService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoknadServiceTest {

    private static final String EIER = "Hans og Grete";
    private static final String BEHANDLINGSID = "123";

    @Mock
    private HenvendelseService henvendelseService;
    @Mock
    private OppgaveHandterer oppgaveHandterer;
    @Mock
    private SystemdataUpdater systemdataUpdater;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private InnsendingService innsendingService;
    @Mock
    private TextService textService;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private SoknadService soknadService;


    @SuppressWarnings("unchecked")
    @BeforeEach
    public void before() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void skalStarteSoknad() {
        when(henvendelseService.startSoknad(anyString())).thenReturn("123");
        soknadService.startSoknad("");

        String bruker = SubjectHandler.getUserId();
        verify(henvendelseService).startSoknad(bruker);
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).opprettSoknad(argument.capture(), eq(bruker));
        List<JsonOkonomibekreftelse> bekreftelser = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
        assertThat(bekreftelser.stream().anyMatch(bekreftelse -> harBekreftelseFor(bekreftelse, UTBETALING_SKATTEETATEN_SAMTYKKE))).isFalse();
        assertThat(bekreftelser.stream().anyMatch(bekreftelse -> harBekreftelseFor(bekreftelse, BOSTOTTE_SAMTYKKE))).isFalse();
    }

    private boolean harBekreftelseFor(JsonOkonomibekreftelse bekreftelse, String bekreftelsesType) {
        return bekreftelse.getVerdi() && bekreftelse.getType().equalsIgnoreCase(bekreftelsesType);
    }

    @Test
    void skalSendeSoknad() {
        String testType = "testType";
        String testTilleggsinfo = "testTilleggsinfo";
        String testType2 = "testType2";
        String testTilleggsinfo2 = "testTilleggsinfo2";
        List<JsonVedlegg> jsonVedlegg = asList(
                new JsonVedlegg()
                        .withType(testType)
                        .withTilleggsinfo(testTilleggsinfo)
                        .withStatus(Vedleggstatus.LastetOpp.toString()),
                new JsonVedlegg()
                        .withType(testType2)
                        .withTilleggsinfo(testTilleggsinfo2)
                        .withStatus(Vedleggstatus.LastetOpp.toString()));

        String behandlingsId = "123";
        String aktorId = "123456";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(aktorId));
        soknadUnderArbeid.getJsonInternalSoknad().setVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg));
        when(soknadUnderArbeidRepository.hentSoknad(eq(behandlingsId), anyString())).thenReturn(soknadUnderArbeid);

        soknadService.sendSoknad(behandlingsId);

        ArgumentCaptor<SoknadUnderArbeid> soknadUnderArbeidCaptor = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        ArgumentCaptor<VedleggMetadataListe> vedleggCaptor = ArgumentCaptor.forClass(VedleggMetadataListe.class);
        verify(henvendelseService, atLeastOnce()).oppdaterMetadataVedAvslutningAvSoknad(eq(behandlingsId), vedleggCaptor.capture(), soknadUnderArbeidCaptor.capture(), eq(false));
        verify(oppgaveHandterer).leggTilOppgave(eq(behandlingsId), anyString());

        SoknadUnderArbeid capturedSoknadUnderArbeid = soknadUnderArbeidCaptor.getValue();
        assertThat(capturedSoknadUnderArbeid).isEqualTo(soknadUnderArbeid);

        VedleggMetadataListe capturedVedlegg = vedleggCaptor.getValue();
        assertThat(capturedVedlegg.vedleggListe).hasSize(2);
        assertThat(capturedVedlegg.vedleggListe.get(0).filnavn).isEqualTo(testType);
        assertThat(capturedVedlegg.vedleggListe.get(0).skjema).isEqualTo(testType);
        assertThat(capturedVedlegg.vedleggListe.get(0).tillegg).isEqualTo(testTilleggsinfo);
        assertThat(capturedVedlegg.vedleggListe.get(1).filnavn).isEqualTo(testType2);
        assertThat(capturedVedlegg.vedleggListe.get(1).skjema).isEqualTo(testType2);
        assertThat(capturedVedlegg.vedleggListe.get(1).tillegg).isEqualTo(testTilleggsinfo2);
    }

    @Test
    void skalAvbryteSoknad() {
        when(soknadUnderArbeidRepository.hentSoknadOptional(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid()
                        .withBehandlingsId(BEHANDLINGSID)
                        .withVersjon(1L)
                        .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));

        soknadService.avbrytSoknad(BEHANDLINGSID);

        verify(henvendelseService).avbrytSoknad(BEHANDLINGSID, false);
        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }
}