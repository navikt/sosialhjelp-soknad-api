package no.nav.sbl.dialogarena.dokumentinnsending;

import no.nav.modig.core.context.JettySubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.dokumentinnsending.config.SystemProperties;
import org.eclipse.jetty.jaas.JAASLoginService;

import java.io.File;
import java.io.IOException;

import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;
import static no.nav.modig.test.util.FilesAndDirs.TEST_RESOURCES;
import static no.nav.modig.test.util.FilesAndDirs.WEBAPP_SOURCE;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;

public final class StartJettyWeb {

    public static final int PORT = 8181;

    public static void main(String[] args) throws IOException {
        System.setProperty("no.nav.modig.security.sts.url", "http://a34duvw22583.devillo.no:9080/SecurityTokenServiceProvider/");
        // System.setProperty("no.nav.modig.security.sts.url", "http://A34DUVW22302.devillo.no:9080/SecurityTokenServiceProvider/");
        // Eirik Lokal System.setProperty("no.nav.modig.security.sts.url", "http://a34duvw22439.devillo.no:9081/SecurityTokenServiceProvider/");
        System.setProperty("no.nav.modig.security.systemuser.username", "BD05");
        System.setProperty("no.nav.modig.security.systemuser.password", "test");
        System.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        SystemProperties.setFrom("jetty-env.properties");
        System.setProperty("spring.profiles.active", "mock");
        System.setProperty("no.nav.sbl.dialogarena.dokumentinnsending.sslMock", "true");

        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, JettySubjectHandler.class.getName());
        // System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, SubjectHandler.class.getName());
        //SubjectHandlerUtils.setupJettySubjectHandler("***REMOVED***", "Ekstern-bruker", "BD05", "4");

        System.setProperty("java.security.auth.login.config", "src/test/resources/login.conf");
        TestCertificates.setupKeyAndTrustStore();

        JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");

        Jetty jetty = usingWar(WEBAPP_SOURCE)
                .at("dokumentinnsending")
                .withLoginService(jaasLoginService)
                .overrideWebXml(new File(TEST_RESOURCES, "override-web.xml"))
                .sslPort(8500)
                .port(PORT).buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    private StartJettyWeb() {
    }
}