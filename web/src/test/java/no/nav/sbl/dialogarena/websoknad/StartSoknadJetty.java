package no.nav.sbl.dialogarena.websoknad;

import no.nav.modig.core.context.JettySubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.websoknad.config.SystemProperties;
import org.eclipse.jetty.jaas.JAASLoginService;

import java.io.File;
import java.io.IOException;

import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;
import static no.nav.modig.test.util.FilesAndDirs.TEST_RESOURCES;
import static no.nav.modig.test.util.FilesAndDirs.WEBAPP_SOURCE;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;

public final class StartSoknadJetty {

    public static final int PORT = 8181;

    private enum Env {
        Intellij("web/src/test/resources/login.conf"),
        Eclipse("src/test/resources/login.conf");
        private final String loginConf;

        Env(String loginConf) {
            this.loginConf = loginConf;
        }

        String getLoginConf() {
            return loginConf;
        }
    }

    private StartSoknadJetty(Env env) throws Exception {
        configureSecurity();
        configureLocalConfig();
        System.setProperty("java.security.auth.login.config", env.getLoginConf());
        TestCertificates.setupKeyAndTrustStore();

        JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");
        Jetty jetty = usingWar(WEBAPP_SOURCE)
                .at("/sendsoknad")
                .withLoginService(jaasLoginService)
                .overrideWebXml(new File(TEST_RESOURCES, "override-web.xml"))
                .sslPort(8500)
                .port(PORT).buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    private void configureLocalConfig() throws IOException {
        SystemProperties.setFrom("jetty-env.properties");
        System.setProperty("spring.profiles.active", "mock");
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock", "true");
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, JettySubjectHandler.class.getName());
    }

    private void configureSecurity() {
        System.setProperty("no.nav.modig.security.sts.url", "http://es-gw-t.test.internsone.local:9080/SecurityTokenServiceProvider/");
        System.setProperty("no.nav.modig.security.systemuser.username", "BD05");
        System.setProperty("no.nav.modig.security.systemuser.password", "test");
        System.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    }

    private static class Intellij {
        public static void main(String[] args) throws Exception {
            new StartSoknadJetty(Env.Intellij);
        }
    }

    private static class Eclipse {
        public static void main(String[] args) throws Exception {
            new StartSoknadJetty(Env.Eclipse);
        }
    }
}