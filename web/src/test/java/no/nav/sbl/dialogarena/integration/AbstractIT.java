package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;
import org.glassfish.jersey.test.TestProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.path.FilesAndDirs.TEST_RESOURCES;

public abstract class AbstractIT {
    private static final int PORT = 10001;
    private static SoknadsosialhjelpServer jetty;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
        System.setProperty("environment.name", "t1");
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        jetty = new SoknadsosialhjelpServer(PORT, new File(TEST_RESOURCES, "override-web-integration.xml"), "/sendsoknad", buildDataSource("hsqldb.properties"));
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
        setProperty("start.oidc.withmock", "false"); // pga. Testene validerer oidc-filtre
        jetty.start();
    }

    @AfterClass
    public static void afterClass() {
        jetty.jetty.stop.run();
    }

    protected SoknadTester soknadOpprettet() {
        try {
            return SoknadTester.startSoknad();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Kunne ikke opprette søknad");
        }
    }
}
