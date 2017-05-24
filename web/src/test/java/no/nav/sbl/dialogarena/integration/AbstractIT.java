package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.StartSoknadJetty;
import org.glassfish.jersey.test.TestProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.path.FilesAndDirs.TEST_RESOURCES;

public abstract class AbstractIT {
    private static final int PORT = 10001;
    private static StartSoknadJetty jetty;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");

        jetty = new StartSoknadJetty(
                StartSoknadJetty.Env.Intellij,
                new File(TEST_RESOURCES, "override-web-integration.xml"),
                buildDataSource("hsqldb.properties"),
                PORT
        );
        jetty.jetty.start();
    }

    @AfterClass
    public static void afterClass() {
        jetty.jetty.stop.run();
    }

    SoknadTester soknadMedDelstegstatusOpprettet(String dagpengerSkjemaNummer) {
        try {
            return SoknadTester.startSoknad(dagpengerSkjemaNummer)
                    .settDelstegstatus("opprettet")
                    .hentSoknad()
                    .hentFakta();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Kunne ikke opprette søknad");
        }
    }
}
