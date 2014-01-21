package no.nav.sbl.dialogarena.websoknad;

import java.io.File;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import org.eclipse.jetty.jaas.JAASLoginService;

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
}
