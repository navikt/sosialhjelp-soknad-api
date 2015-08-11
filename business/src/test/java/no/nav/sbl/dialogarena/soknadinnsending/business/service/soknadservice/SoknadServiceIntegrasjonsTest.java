package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.OpenAmTokenCredential;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.SluttBruker;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadDataFletterIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadDataFletterIntegrationTestContext.class)
public class SoknadServiceIntegrasjonsTest {
    private final String EN_BEHANDLINGSID = "EN_BEHANDLINGSID";
    WebSoknad soknad;
    String uuid = "uid";
    String skjemaNummer = "";
    long soknadId;

    @Inject
    private SoknadRepository lokalDb;

    @Inject
    private SoknadService soknadService;

    @Inject
    private SendSoknadPortType sendSoknadEndpoint;

    @Inject
    private FilLagerPortType fillagerEndpoint;

    @Inject
    private FillagerService fillagerService;

    @BeforeClass
    public static void beforeClass() throws IOException, NamingException {
//        load("/environment-test.properties");
//        System.setProperty("no.nav.modig.security.sts.url", "dummyvalue");
//        System.setProperty("no.nav.modig.security.systemuser.username", "dummyvalue");
//        System.setProperty("no.nav.modig.security.systemuser.password", "");
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
//        getProperties().setProperty(TILLATMOCK_PROPERTY, DEFAULT_MOCK_TILLATT);
//
//        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
//        builder.bind("jdbc/SoknadInnsendingDS", Mockito.mock(DataSource.class));
//        builder.activate();
    }

    @Before
    public void beforeEach() {
        WSBehandlingsId wsBehandlingsId = new WSBehandlingsId().withBehandlingsId(EN_BEHANDLINGSID);
        when(sendSoknadEndpoint.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(wsBehandlingsId);
    }

    @Test
    public void henterTemakode_FOR_forForeldrepenger() {
        skjemaNummer = "NAV 14-05.06";
        opprettOgPersisterSoknad("behId", "aktor");
        SoknadStruktur soknadStruktur = soknadService.hentSoknadStruktur(skjemaNummer);
        assertThat(soknadStruktur.getTemaKode(), equalTo("FOR"));
    }

    @Test
    public void startSoknadHenterBehandlingsIdFraHenvendelse() {
        ((ThreadLocalSubjectHandler) getSubjectHandler()).setSubject(getSubject());

        String behandlingsId = soknadService.startSoknad("NAV 14-05.06");

        assertThat(behandlingsId, equalTo(EN_BEHANDLINGSID));
    }

    @Test
    public void hentSoknadFraLokalDbReturnererPopulertSoknad() {
        skjemaNummer = "NAV 14-05.06";
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);

        assertThat(webSoknad.getBrukerBehandlingId(), equalTo(EN_BEHANDLINGSID));
        assertThat(webSoknad.getAktoerId(), equalTo("aktor"));
        assertThat(webSoknad.getUuid(), equalTo(uuid));
        assertThat(webSoknad.getDelstegStatus(), equalTo(DelstegStatus.OPPRETTET));
        assertThat(webSoknad.getskjemaNummer(), equalTo(skjemaNummer));
    }

    @Test
    public void settDelstegPersistererNyttDelstegTilDb() {
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.settDelsteg(EN_BEHANDLINGSID, DelstegStatus.SAMTYKKET);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getDelstegStatus(), equalTo(DelstegStatus.SAMTYKKET));
    }

    @Test
    public void settJournalforendeEnhetPersistererNyJournalforendeEnhetTilDb(){
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.settJournalforendeEnhet(EN_BEHANDLINGSID, "NAV UTLAND");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getJournalforendeEnhet(), equalTo("NAV UTLAND"));
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb(){
        Long soknadId = opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad, nullValue());
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraHenvendelse(){
        opprettOgPersisterSoknad(EN_BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(EN_BEHANDLINGSID);

        List<WSInnhold> filer = fillagerService.hentFiler(EN_BEHANDLINGSID);
        assertTrue(filer.isEmpty());
    }

    @Test
    public void avbrytSoknadAvbryterSoknadenIHenvendelse(){
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.avbrytSoknad(behandlingsId);

        verify(sendSoknadEndpoint).avbrytSoknad(behandlingsId);
    }

    @Test
    public void sendSoknadSkalLagreEnFilTilHenvendelseHvisForeldrepenger() {
        ((ThreadLocalSubjectHandler) getSubjectHandler()).setSubject(getSubject());
        skjemaNummer = "NAV 14-05.06";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerEndpoint, times(1)).lagre(eq(behandlingsId), any(String.class), any(String.class), any(DataHandler.class));
    }

    @Test
    public void sendSoknadSkalLagreToFilerTilHenvendelseHvisTilleggsstonader() {
        ((ThreadLocalSubjectHandler) getSubjectHandler()).setSubject(getSubject());
        skjemaNummer = "NAV 08-14.01";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerEndpoint, times(2)).lagre(eq(behandlingsId), any(String.class), any(String.class), any(DataHandler.class));
    }

    private Subject getSubject() {
        Subject subject = new Subject();
        subject.getPrincipals().add(SluttBruker.eksternBruker("98989898989"));
        subject.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        subject.getPublicCredentials().add(new OpenAmTokenCredential("98989898989-4"));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        return subject;
    }

    private String nyBehandlnigsId() {
        return UUID.randomUUID().toString();
    }

    private Long opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer).medOppretteDato(now());
        soknadId = lokalDb.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    @After
    public void afterEach(){
        lokalDb.slettSoknad(soknadId);
    }

}