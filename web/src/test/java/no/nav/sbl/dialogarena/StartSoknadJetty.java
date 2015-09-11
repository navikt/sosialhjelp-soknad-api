package no.nav.sbl.dialogarena;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import org.eclipse.jetty.jaas.JAASLoginService;

import java.io.File;
import java.io.IOException;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;
import static no.nav.modig.test.util.FilesAndDirs.TEST_RESOURCES;
import static no.nav.modig.test.util.FilesAndDirs.WEBAPP_SOURCE;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.config.SystemProperties.setFrom;

public final class StartSoknadJetty {

    public static final int PORT = 8181;

    private enum Env {
        Intellij("web/src/test/resources/login.conf"),
        Eclipse("src/test/resources/login.conf");
        private final String loginConf;

        Env (String loginConf) {
            this.loginConf = loginConf;
        }

        String getLoginConf() {
            return loginConf;
        }
    }

    private StartSoknadJetty(Env env) throws Exception {
        configureSecurity();
        configureLocalConfig();
        disableBatch();
        setProperty("java.security.auth.login.config", env.getLoginConf());
        TestCertificates.setupTemporaryKeyStore(this.getClass().getResourceAsStream("/keystore.jks"), "devillokeystore1234");
        TestCertificates.setupTemporaryTrustStore(this.getClass().getResourceAsStream("/truststore.jts"), "changeit");

        JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");
        Jetty jetty = usingWar(WEBAPP_SOURCE)
                .at("/sendsoknad")
                .withLoginService(jaasLoginService)
                .overrideWebXml(new File(TEST_RESOURCES, "override-web.xml"))
                .sslPort(8500)
                .addDatasource(buildDataSource(), "jdbc/SoknadInnsendingDS")
                .port(PORT).buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    private void disableBatch() {
        setProperty("sendsoknad.batch.enabled", "false");
    }

    private void configureLocalConfig() throws IOException {
        setFrom("environment-test.properties");
        setProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock", "true");
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
    }

    private void configureSecurity() {
        setProperty("no.nav.modig.security.sts.url", "http://e34jbsl00713.devillo.no:8080/SecurityTokenServiceProvider/"); // Microscopium U1
        setProperty("no.nav.modig.security.systemuser.username", "srvSendsoknad");
        setProperty("no.nav.modig.security.systemuser.password", "test");
        setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    }

    // For å logge inn lokalt må du sette cookie i selftesten: document.cookie="nav-esso=***REMOVED***-4; path=/sendsoknad/"

    @SuppressWarnings("unused")
    private static class Intellij {
        public static void main(String[] args) throws Exception {
            new StartSoknadJetty(Env.Intellij);
        }
    }

    @SuppressWarnings("unused")
    private static class Eclipse {
        public static void main(String[] args) throws Exception {
            new StartSoknadJetty(Env.Eclipse);
        }
    }
}