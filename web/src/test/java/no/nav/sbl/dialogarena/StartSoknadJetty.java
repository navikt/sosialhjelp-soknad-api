package no.nav.sbl.dialogarena;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import org.eclipse.jetty.jaas.JAASLoginService;

import javax.sql.DataSource;
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
import static no.nav.sbl.dialogarena.config.SystemProperties.setFrom;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;

public final class StartSoknadJetty {

    public static final int PORT = 8181;
    public final Jetty jetty;

    public StartSoknadJetty(Env env, File overrideWebXmlFile, DataSource dataSource) throws Exception {
        this(env, overrideWebXmlFile, dataSource, PORT);

    }


    public StartSoknadJetty(Env env, File overrideWebXmlFile, DataSource dataSource, int port) throws Exception {
        configureSecurity();
        configureLocalConfig();
        disableBatch();
        setProperty("java.security.auth.login.config", env.getLoginConf());
        TestCertificates.setupTemporaryKeyStore(this.getClass().getResourceAsStream("/keystore.jks"), "devillokeystore1234");
        TestCertificates.setupTemporaryTrustStore(this.getClass().getResourceAsStream("/truststore.jts"), "changeit");

        JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");
        jetty = usingWar(WEBAPP_SOURCE)
                .at("/sendsoknad")
                .withLoginService(jaasLoginService)
                .overrideWebXml(overrideWebXmlFile)
                .sslPort(port + 100)
                .addDatasource(dataSource, "jdbc/SoknadInnsendingDS")
                .port(port).buildJetty();
    }

    public void startAndWaitForKeypress() {
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

    public void startAndDo(Runnable test) {
        jetty.startAnd(first(test).then(jetty.stop));
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

    public enum Env {
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

    // For å logge inn lokalt må du sette cookie i selftesten: document.cookie="nav-esso=***REMOVED***-4; path=/sendsoknad/"

    @SuppressWarnings("unused")
    private static class Intellij {
        public static void main(String[] args) throws Exception {
            setFrom("environment-test.properties");
            new StartSoknadJetty(Env.Intellij, new File(TEST_RESOURCES, "override-web.xml"), buildDataSource()).startAndWaitForKeypress();
        }
    }

    @SuppressWarnings("unused")
    private static class Eclipse {
        public static void main(String[] args) throws Exception {
            setFrom("environment-test.properties");
            new StartSoknadJetty(Env.Eclipse, new File(TEST_RESOURCES, "override-web.xml"), buildDataSource()).startAndWaitForKeypress();
            ;
        }
    }
}