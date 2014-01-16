package no.nav.sbl.dialogarena.websoknad;

<<<<<<< HEAD
import no.nav.modig.core.context.JettySubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.websoknad.config.SystemProperties;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.jaas.JAASLoginService;

=======
import java.io.File;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import org.eclipse.jetty.jaas.JAASLoginService;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
>>>>>>> master
import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;
import static no.nav.modig.test.util.FilesAndDirs.TEST_RESOURCES;
import static no.nav.modig.test.util.FilesAndDirs.WEBAPP_SOURCE;
<<<<<<< HEAD
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;

public final class StartJettyIntegration {

    public static final int PORT = 8181;

    public static void main(String[] args) throws IOException {
    	System.setProperty("no.nav.modig.security.sts.url", "http://es-gw-t.test.internsone.local:9080/SecurityTokenServiceProvider/");
        //System.setProperty("no.nav.modig.security.sts.url", "http://A34DUVW22302.devillo.no:9080/SecurityTokenServiceProvider/");
        //System.setProperty("no.nav.modig.security.sts.url", "http://a34duvw22439.devillo.no:9080/SecurityTokenServiceProvider/");
    	//System.setProperty("no.nav.modig.security.sts.url", "https://sts-t8.oera-t.local/SecurityTokenServiceProvider");
        System.setProperty("no.nav.modig.security.systemuser.username", "BD05");
        System.setProperty("no.nav.modig.security.systemuser.password", "test");
        System.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        SystemProperties.setFrom("jetty-env.properties");
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, JettySubjectHandler.class.getName());
//        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, SubjectHandler.class.getName());
        //SubjectHandlerUtils.setupJettySubjectHandler("01015245464", "Ekstern-bruker", "BD05", "4");

        System.setProperty("java.security.auth.login.config", "src/test/resources/login.conf");
        TestCertificates.setupKeyAndTrustStore();

        JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");

        Jetty jetty = usingWar(WEBAPP_SOURCE)
                .at("dokumentinnsending")
                .withLoginService(jaasLoginService)
                .overrideWebXml(new File(TEST_RESOURCES, "override-web.xml"))
                .port(PORT).buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    private StartJettyIntegration() {
    }

=======
import static no.nav.modig.testcertificates.TestCertificates.setupKeyAndTrustStore;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.websoknad.config.SystemProperties.setFrom;

public final class StartJettyIntegration {

    public static void main(String[] args) throws Exception {
        setupKeyAndTrustStore();
        setProperty("disable.ssl.cn.check", "true");
        setProperty("java.security.auth.login.config", "web/src/test/resources/login.conf");
        setProperty("no.nav.modig.security.sts.url", "https://e34jbsl00833.devillo.no:8443/SecurityTokenServiceProvider");
        setProperty("no.nav.modig.security.systemuser.username", "BD05");
        setProperty("no.nav.modig.security.systemuser.password", "test");
        setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
        setFrom("jetty-env.properties");
        setProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock", "true");
        setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "false");
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());

        JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");
        Jetty jetty = usingWar(WEBAPP_SOURCE)
                .at("/sendsoknad")
                .withLoginService(jaasLoginService)
                .overrideWebXml(new File(TEST_RESOURCES, "override-integration-web.xml"))
                .sslPort(8500)
                .addDatasource(buildDataSource(), "jdbc/SoknadInnsendingDS")
                .port(8181)
                .buildJetty();

        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }
>>>>>>> master
}
