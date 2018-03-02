package no.nav.sbl.dialogarena.server;

import static java.lang.System.setProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

//import org.eclipse.jetty.jaas.JAASLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import no.nav.modig.core.context.StaticSubjectHandler;

public class SoknadsosialhjelpServer {

    private static final Logger log = LoggerFactory.getLogger(SoknadsosialhjelpServer.class);
    //public static final int PORT = 8181;
    public static final int PORT = 8080;
    public final Jetty jetty;

    public SoknadsosialhjelpServer(DataSource dataSource) throws Exception {
        configureSecurity();
        configureLocalConfig();
        disableBatch();
        
        setProperty("java.security.auth.login.config", "login.conf");
        //JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        //jaasLoginService.setLoginModuleName("openam");
        jetty = new Jetty.JettyBuilder()
                .at("/soknadsosialhjelp-server")
                //.withLoginService(jaasLoginService)
                //.overrideWebXml(new File("src/main/resources/override-web.xml"))
                //.sslPort(PORT + 100)
                .addDatasource(dataSource, "jdbc/SoknadInnsendingDS")
                .port(PORT)
                .buildJetty();
    }
    
    public void start() {
        jetty.start();
    }
    
    public void mapNaisProperties() throws IOException {
        final Properties props = readProperties("naisPropertyMapping.properties");

        for (String env : props.stringPropertyNames()) {
            final String interntNavn = props.getProperty(env);
            final String value = System.getenv(env);
            if (value != null) {
                System.setProperty(interntNavn, value);
            }
        }
    }

    private void disableBatch() {
        setProperty("sendsoknad.batch.enabled", "false");
    }

    private void configureLocalConfig() throws IOException {
        setFrom("environment-test.properties");
        mapNaisProperties();
        setProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock", "true");
        setProperty(StaticSubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
    }
    
    public static void setFrom(String resource) throws IOException {
        final Properties props = readProperties(resource);
        
        for (String entry : props.stringPropertyNames()) {
            System.setProperty(entry, props.getProperty(entry));
        }
    }

    private static Properties readProperties(String resource) throws IOException {
        Properties props = new Properties();
        InputStream inputStream = SoknadsosialhjelpServer.class.getClassLoader().getResourceAsStream(resource);
        props.load(inputStream);
        return props;
    }

    private void configureSecurity() {
        setProperty("no.nav.modig.security.sts.url", "http://e34jbsl01634.devillo.no:8080/SecurityTokenServiceProvider"); // Microscopium U1
        setProperty("no.nav.modig.security.systemuser.username", "srvSendsoknad");
        setProperty("no.nav.modig.security.systemuser.password", "test");
        setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    }

    // For å logge inn lokalt må du sette cookie i selftesten: document.cookie="nav-esso=***REMOVED***-4; path=/sendsoknad/"
    
    public static DataSource buildDataSource(String propertyFileName) throws IOException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //
        //dataSource.setSuppressClose(true);
        Properties env = dbProperties(propertyFileName);
        dataSource.setDriverClassName(env.getProperty("db.driverClassName"));
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));
        return dataSource;
    }
    
    private static Properties dbProperties(String propertyFileName) throws IOException {
        Properties env = new Properties();
        env.load(SoknadsosialhjelpServer.class.getResourceAsStream("/" + propertyFileName));
        return env;
    }

    
    public static void main(String[] args) {
        try {
            final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(buildDataSource("oracledb.properties"));
            server.start();
        } catch (Exception e) {
            log.error("Kunne ikke starte opp soknadsosialhjelp-server", e);
        }
    }
}
