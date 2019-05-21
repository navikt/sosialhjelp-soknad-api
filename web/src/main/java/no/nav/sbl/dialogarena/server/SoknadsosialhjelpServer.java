package no.nav.sbl.dialogarena.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.sbl.dialogarena.mock.MockSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;

public class SoknadsosialhjelpServer {

    private static final Logger log = LoggerFactory.getLogger(SoknadsosialhjelpServer.class);
    public static final int PORT = isRunningOnHeroku() ? Integer.parseInt(System.getenv("PORT")) : 8080;
    public final Jetty jetty;


    public SoknadsosialhjelpServer() throws Exception {
        this(PORT, null, "/soknadsosialhjelp-server", null);
    }

    public SoknadsosialhjelpServer(int listenPort, File overrideWebXmlFile, String contextPath, DataSource dataSource) throws Exception {
        configure();

        if (ServiceUtils.isRunningInProd() && MockUtils.isTillatMockRessurs()) {
            throw new Error("tillatMockRessurs har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
        }

        if (MockUtils.isTillatMockRessurs()) {
            dataSource = DatabaseTestContext.buildDataSource("hsqldb.properties");
        }
        final DataSource ds = (dataSource != null) ? dataSource : buildDataSource();

        if (isRunningOnNais() && !MockUtils.isTillatMockRessurs()) {
            databaseSchemaMigration(ds);
        }

        final String loginConfFile = SoknadsosialhjelpServer.class.getClassLoader().getResource("login.conf").getFile();
        setProperty("java.security.auth.login.config", loginConfFile);
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

        if (isRunningOnNais()) {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(jetty));
        }
    }

    private void databaseSchemaMigration(final DataSource ds) {
        log.debug("Running Flyway migration.");
        final Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        final int migrations = flyway.migrate();
        log.info("Flyway migration successfully executed. Number of new applied migrations: " + migrations);
    }


    public void start() {
        jetty.start();
    }

    private void configure() throws IOException {
        Locale.setDefault(Locale.forLanguageTag("nb-NO"));
        if (isRunningAsTestAppWithMockingActivated() || MockUtils.isTillatMockRessurs()){
            log.info("Running with mocking activated. Totally isolated.");
            setFrom("environment/mock-test.properties");
            if (!MockUtils.isTillatMockRessurs()) {
                throw new Error("Mocking må være aktivert når applikasjonen skal kjøre isolert.");
            }
        } else if (isRunningOnNais()) {
            mapNaisProperties();
            setFrom("environment/environment.properties");
        } else {
            log.info("Running with DEVELOPER (local) setup.");
            configureLocalEnvironment();
        }

        if (MockUtils.isTillatMockRessurs()){
            SubjectHandler.setSubjectHandlerService(new MockSubjectHandlerService());
        } else {
            SubjectHandler.setSubjectHandlerService(new OidcSubjectHandlerService());
        }
        System.setProperty(SUBJECTHANDLER_KEY, ThreadLocalSubjectHandler.class.getName()); // pga SaksoversiktMetadataRessurs og applikasjon som kjører uten oidc.

    }

    private boolean isRunningAsTestAppWithMockingActivated() {
        return System.getenv("dockerWithDefaultMockActivated") != null && Boolean.parseBoolean(System.getenv("dockerWithDefaultMockActivated"));
    }


    public static boolean isRunningOnHeroku(){
        return System.getenv("HEROKU") != null && Boolean.parseBoolean(System.getenv("HEROKU"));
    }

    private void mapNaisProperties() throws IOException {
        final Properties props = readProperties("naisPropertyMapping.properties", true);

        for (String env : props.stringPropertyNames()) {
            final String interntNavn = props.getProperty(env);
            final String value = findVariableValue(env);
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
    
    public static void setFrom(String resource) throws IOException {
        setFrom(resource, true);
    }

    public static void setFrom(String resource, boolean required) throws IOException {
        final Properties props = readProperties(resource, required);

        updateJavaProperties(props);
    }

    private static void updateJavaProperties(final Properties props) {
        for (String entry : props.stringPropertyNames()) {
            final String value = withEnvironmentVariableExpansion(props.getProperty(entry));
            System.setProperty(entry, value);
        }
    }

    static String withEnvironmentVariableExpansion(String value) {
        if (value == null) {
            return null;
        }

        final Pattern p = Pattern.compile("\\$\\{([^}]*)\\}");
        final Matcher m = p.matcher(value);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final String variableName = m.group(1);
            final String replacement = Matcher.quoteReplacement(findVariableValue(variableName));
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        
        return sb.toString();
    }

    private static String findVariableValue(final String variableName) {
        final String envValue = System.getenv(variableName);
        if (envValue != null) {
            return envValue;
        }
        final String propValue = System.getProperty(variableName);
        if (propValue != null) {
            return propValue;
        }
        
        throw new IllegalStateException("Kunne ikke finne referert variabel med navn: " + variableName);
    }

    private static Properties readProperties(String resource, boolean required) throws IOException {
        Properties props = new Properties();
        InputStream inputStream = SoknadsosialhjelpServer.class.getClassLoader().getResourceAsStream(resource);
        if (inputStream == null) {
            if (required) {
                throw new IllegalStateException("Kunne ikke finne propertiesfil: " + resource);
            } else {
                return props;
            }
        }
        props.load(inputStream);
        return props;
    }

    private void configureLocalEnvironment() throws IOException {
        setFrom("environment-test.properties");
        updateJavaProperties(readProperties("oracledb.properties", false));
    }

    private static DataSource buildDataSource() throws IOException {

        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(System.getProperty("db.url"));
        config.setUsername(System.getProperty("db.username"));
        config.setPassword(System.getProperty("db.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);

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
