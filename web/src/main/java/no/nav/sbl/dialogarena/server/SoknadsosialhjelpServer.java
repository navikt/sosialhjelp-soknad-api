package no.nav.sbl.dialogarena.server;

import static java.lang.System.setProperty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SoknadsosialhjelpServer {

    private static final Logger log = LoggerFactory.getLogger(SoknadsosialhjelpServer.class);
    public static final int PORT = 8080;
    public final Jetty jetty;

    
    public SoknadsosialhjelpServer() throws Exception {
        this(PORT, null, "/soknadsosialhjelp-server", null);
    }
    
    public SoknadsosialhjelpServer(int listenPort, File overrideWebXmlFile, String contextPath, DataSource dataSource) throws Exception {
        configure();
        
        final DataSource ds = (dataSource != null) ? dataSource : buildDataSource();
        
        setProperty("java.security.auth.login.config", "login.conf");
        final JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
        jaasLoginService.setLoginModuleName("openam");
        jetty = new Jetty.JettyBuilder()
                .at(contextPath)
                .withLoginService(jaasLoginService)
                .overrideWebXml(overrideWebXmlFile)
                //.sslPort(PORT + 100)
                .addDatasource(ds, "jdbc/SoknadInnsendingDS")
                .port(listenPort)
                .buildJetty();
    }
    
    
    public void start() {
        jetty.start();
    }
    
    private void configure() throws IOException {
        if (isRunningOnNais()) {
            mapNaisProperties();
            readEnvironmentProperties();
            System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        } else {
            configureLocalSecurity();
        }
        
        disableBatch();
    }
    
    private void mapNaisProperties() throws IOException {
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
    
    private static boolean isRunningOnNais() {
        return determineEnvironment() != null;
    }
    
    private static String determineEnvironment() {
        final String env = System.getenv("FASIT_ENVIRONMENT_NAME");
        if (env == null || env.trim().equals("")) {
            return null;
        }
        return env;
    }
    
    private void readEnvironmentProperties() throws IOException {
        final String env = determineEnvironment();
        setFrom("environment/" + env + "/environment.properties");
        log.info("Lastet inn oppsett for miljø: " + env);
    }

    public static void setFrom(String resource) throws IOException {
        final Properties props = readProperties(resource);
        
        updateJavaProperties(props);
    }

    private static void updateJavaProperties(final Properties props) {
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

    private void configureLocalSecurity() throws IOException {
        setFrom("environment-test.properties");
        updateJavaProperties(readProperties("oracledb.properties"));
    }

    // For å logge inn lokalt må du sette cookie i selftesten: document.cookie="nav-esso=***REMOVED***-4; path=/sendsoknad/"
    
    private static DataSource buildDataSource() throws IOException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        dataSource.setUrl(System.getProperty("db.url"));
        dataSource.setUsername(System.getProperty("db.username"));
        dataSource.setPassword(System.getProperty("db.password"));
        return dataSource;
    }
    
    public static void main(String[] args) {
        try {
            final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer();
            server.start();
        } catch (Exception e) {
            log.error("Kunne ikke starte opp soknadsosialhjelp-server", e);
        }
    }
}
