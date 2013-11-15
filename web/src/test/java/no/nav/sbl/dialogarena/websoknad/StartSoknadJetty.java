package no.nav.sbl.dialogarena.websoknad;

import no.nav.modig.core.context.StaticSubjectHandler;
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
import static no.nav.modig.testcertificates.TestCertificates.setupKeyAndTrustStore;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.websoknad.config.SystemProperties.setFrom;

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
        setProperty("java.security.auth.login.config", env.getLoginConf());
        setupKeyAndTrustStore();

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

    private void configureLocalConfig() throws IOException {
        setFrom("jetty-env.properties");
        setProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock", "true");
        setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "false");
        //setProperty(SUBJECTHANDLER_KEY, JettySubjectHandler.class.getName());
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
    }

    private void configureSecurity() {
        setProperty("no.nav.modig.security.sts.url", "http://es-gw-t.test.internsone.local:9080/SecurityTokenServiceProvider/");
        setProperty("no.nav.modig.security.systemuser.username", "BD05");
        setProperty("no.nav.modig.security.systemuser.password", "test");
        setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    }

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