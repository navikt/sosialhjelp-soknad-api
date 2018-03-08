package no.nav.sbl.dialogarena.integration;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.path.FilesAndDirs.TEST_RESOURCES;

import java.io.File;

import org.glassfish.jersey.test.TestProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

public abstract class AbstractIT {
    private static final int PORT = 10001;
    private static SoknadsosialhjelpServer jetty;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        jetty = new SoknadsosialhjelpServer(PORT, new File(TEST_RESOURCES, "override-web.xml"), "/sendsoknad", buildDataSource("hsqldb.properties"));
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
        setProperty(StaticSubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        jetty.start();
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
