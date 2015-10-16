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
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import java.io.IOException;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadDataFletterIntegrationTestContext.class)
public class SoknadServiceIntegrasjonsTest {
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
        WSBehandlingsId wsBehandlingsId = new WSBehandlingsId().withBehandlingsId("bhid");
        when(sendSoknadEndpoint.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(wsBehandlingsId);

        String behandlingsId = soknadService.startSoknad("NAV 14-05.06");

        assertThat(behandlingsId, equalTo("bhid"));
    }

    private Subject getSubject() {
        Subject subject = new Subject();
        subject.getPrincipals().add(SluttBruker.eksternBruker("98989898989"));
        subject.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        subject.getPublicCredentials().add(new OpenAmTokenCredential("98989898989-4"));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        return subject;
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

}