package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.StartSoknadJetty;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;

import static no.nav.modig.test.util.FilesAndDirs.TEST_RESOURCES;
import static no.nav.sbl.dialogarena.config.IntegrationConfig.getMocked;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class SoknadActionIntegrationTests {
    private static final int PORT = 10001;
    static {
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
    }

    @Test
    public void fullSoknad() throws Exception {
        System.setProperty("spring.profiles.active", "integration");
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
        StartSoknadJetty jetty = new StartSoknadJetty(StartSoknadJetty.Env.Intellij, new File(TEST_RESOURCES, "override-web-integration.xml"), buildDataSource("hsqldb.properties"), PORT);
        jetty.jetty.start();

        SendSoknadPortType soknad = getMocked("sendSoknadEndpoint");
        when(soknad.startSoknad(any(WSStartSoknadRequest.class))).thenAnswer(new Answer<WSBehandlingsId>() {
            @Override
            public WSBehandlingsId answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new WSBehandlingsId().withBehandlingsId("TEST-123");
            }
        });
        SoknadTester.startSoknad("INTEGRATION-1")
                .settDelstegstatus("opprettet")
                .hentSoknad()
                .print()
                .hentFakta()
                .print()
                .updateFaktum("faktum1").withValue("true").utforEndring()
                .updateFaktum("faktum1.faktum2").withValue("true").utforEndring()
                .updateFaktum("faktum1.faktum2.faktum1").withValue("true").utforEndring()
                .updateFaktum("faktum1.faktum2.faktum2").withValue("false").utforEndring()
                .settDelstegstatus("vedlegg")
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("v1", "v3")
                .soknad()
                .settDelstegstatus("vedlegg")
                .sendInn();


        jetty.jetty.stop.run();

    }
}
