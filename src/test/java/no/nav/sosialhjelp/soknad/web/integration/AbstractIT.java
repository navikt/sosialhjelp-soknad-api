package no.nav.sosialhjelp.soknad.web.integration;

import no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer;
import org.glassfish.jersey.test.TestProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.test.path.FilesAndDirs.TEST_RESOURCES;
import static no.nav.sosialhjelp.soknad.business.db.config.DatabaseTestContext.buildDataSource;

public abstract class AbstractIT {
    private static final int PORT = 10001;
    private static SoknadsosialhjelpServer jetty;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("no.nav.sosialhjelp.soknad.hsqldb", "true");
        System.setProperty("environment.name", "test");
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        jetty = new SoknadsosialhjelpServer(PORT, new File(TEST_RESOURCES, "override-web-integration.xml"), "/sendsoknad", buildDataSource("hsqldb.properties"));
        System.setProperty("no.nav.sosialhjelp.soknad.hsqldb", "true");
        setProperty("start.oidc.withmock", "false"); // pga. Testene validerer oidc-filtre
        jetty.start();
    }

    @AfterClass
    public static void afterClass() {
        jetty.jetty.stop.run();
        System.clearProperty("environment.name");
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
