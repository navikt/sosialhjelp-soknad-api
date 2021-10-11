package no.nav.sosialhjelp.soknad.web.integration;

import no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;

import static java.lang.System.setProperty;

public abstract class AbstractIT {
    private static final int PORT = 10001;
    private static SoknadsosialhjelpServer jetty;

    @BeforeAll
    public static void beforeClass() throws Exception {
        System.setProperty("environment.name", "test");
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        // dummy proxy url
        System.setProperty("HTTPS_PROXY", "http://localhost:" + PORT);
        jetty = new SoknadsosialhjelpServer(PORT, new File("src/test/resources/override-web-integration.xml"), "/sosialhjelp/soknad-api");
        setProperty("start.oidc.withmock", "false"); // pga. Testene validerer oidc-filtre
        jetty.start();
    }

    @AfterAll
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
