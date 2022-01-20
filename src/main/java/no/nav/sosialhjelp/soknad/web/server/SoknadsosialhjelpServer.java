//package no.nav.sosialhjelp.soknad.web.server;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import no.nav.sosialhjelp.soknad.business.db.config.DatabaseTestContext;
//import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
//import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
//import org.flywaydb.core.Flyway;
//import org.flywaydb.core.api.output.MigrateResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.sql.DataSource;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Locale;
//import java.util.Properties;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static java.lang.System.setProperty;
//
//public class SoknadsosialhjelpServer {
//
//    private static final Logger log = LoggerFactory.getLogger(SoknadsosialhjelpServer.class);
//    public static final int PORT = 8080;
//    public final Jetty jetty;
//
//    public SoknadsosialhjelpServer() throws Exception {
//        this(PORT, null, "/sosialhjelp/soknad-api");
//    }
//
//    public SoknadsosialhjelpServer(int listenPort, File overrideWebXmlFile, String contextPath) throws Exception {
//        configure();
//
//        if (!ServiceUtils.isNonProduction() && MockUtils.isMockAltProfil()) {
//            throw new Error("mockAltProfil har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
//        }
//
//        if (!ServiceUtils.isNonProduction() && MockUtils.isRunningWithInMemoryDb()) {
//            throw new Error("no.nav.sosialhjelp.soknad.hsqldb har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
//        }
//
//        if (!ServiceUtils.isNonProduction() && (MockUtils.isAlltidHentKommuneInfoFraNavTestkommune() || MockUtils.isAlltidSendTilNavTestkommune())) {
//            throw new Error("Alltid send eller hent fra NavTestkommune er satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.");
//        }
//
//        final DataSource ds = MockUtils.isRunningWithInMemoryDb() ? DatabaseTestContext.buildDataSource("hsqldb.properties") : buildDataSource();
//
//        if (isRunningOnNais() && !MockUtils.isMockAltProfil()) {
//            databaseSchemaMigration(ds);
//        }
//
//        jetty = new Jetty.JettyBuilder()
//                .at(contextPath)
//                .overrideWebXml(overrideWebXmlFile)
//                .addDatasource(ds, "jdbc/SoknadInnsendingDS")
//                .port(listenPort)
//                .buildJetty();
//
//        if (isRunningOnNais()) {
//            Runtime.getRuntime().addShutdownHook(new ShutdownHook(jetty));
//        }
//    }
//
//    private void databaseSchemaMigration(final DataSource ds) {
//        log.debug("Running Flyway migration.");
//        final Flyway flyway = Flyway.configure().dataSource(ds).load();
//        MigrateResult migrateResult = flyway.migrate();
//        log.info("Flyway migration successfully executed. Number of new applied migrations: {}", migrateResult.migrationsExecuted);
//    }
//
//    public void start() {
//        jetty.start();
//    }
//
//    private void configure() throws IOException {
//        Locale.setDefault(Locale.forLanguageTag("nb-NO"));
//        if (MockUtils.isMockAltProfil()) {
//            log.info("Running with mock-alt activated.");
//            setFrom("environment/environment-mock-alt.properties");
//        } else if (isRunningOnNais()) {
//            setFrom("environment/environment.properties");
//        } else {
//            log.info("Running with DEVELOPER (local) setup.");
//            configureLocalEnvironment();
//        }
//    }
//
//    private void disableBatch() {
//        setProperty("sendsoknad.batch.enabled", "false");
//    }
//
//    private static boolean isRunningOnNais() {
//        return determineEnvironment() != null;
//    }
//
//    private static String determineEnvironment() {
//        final String env = System.getenv("ENVIRONMENT_NAME");
//        if (env == null || env.trim().equals("")) {
//            return null;
//        }
//        return env;
//    }
//
//    public static void setFrom(String resource) throws IOException {
//        setFrom(resource, true);
//    }
//
//    public static void setFrom(String resource, boolean required) throws IOException {
//        final Properties props = readProperties(resource, required);
//        updateJavaProperties(props, required);
//    }
//
//    private static void updateJavaProperties(final Properties props, boolean required) {
//        for (String entry : props.stringPropertyNames()) {
//            final String value = withEnvironmentVariableExpansion(props.getProperty(entry), required);
//            System.setProperty(entry, value);
//        }
//    }
//
//    static String withEnvironmentVariableExpansion(String value, boolean required) {
//        if (value == null) {
//            return null;
//        }
//
//        final Pattern p = Pattern.compile("\\$\\{([^}:]*):*([^}]*)\\}"); // Matches and groups properties on this form ${ENV_VAR:https://env.var}. To simulate same logic as in spring boot.
//        final Matcher m = p.matcher(value);
//        final StringBuilder sb = new StringBuilder();
//        while (m.find()) {
//            final String variableName = m.group(1);
//            String replacement = m.group(2);
//            String variableValue = findVariableValue(variableName, required && replacement.isEmpty());
//            if (variableValue != null) {
//                replacement = Matcher.quoteReplacement(variableValue);
//            }
//            m.appendReplacement(sb, replacement);
//        }
//        m.appendTail(sb);
//
//        return sb.toString();
//    }
//
//    private static String findVariableValue(final String variableName, boolean required) {
//        final String envValue = System.getenv(variableName);
//        if (envValue != null) {
//            return envValue;
//        }
//        final String propValue = System.getProperty(variableName);
//        if (propValue != null) {
//            return propValue;
//        }
//        if (required) throw new IllegalStateException("Kunne ikke finne referert variabel med navn: " + variableName);
//        return null;
//    }
//
//    private static Properties readProperties(String resource, boolean required) throws IOException {
//        Properties props = new Properties();
//        InputStream inputStream = SoknadsosialhjelpServer.class.getClassLoader().getResourceAsStream(resource);
//        if (inputStream == null) {
//            if (required) {
//                throw new IllegalStateException("Kunne ikke finne propertiesfil: " + resource);
//            } else {
//                return props;
//            }
//        }
//        props.load(inputStream);
//        return props;
//    }
//
//    private void configureLocalEnvironment() throws IOException {
//        setFrom("environment-test.properties", false);
//        updateJavaProperties(readProperties("oracledb.properties", false), false);
//    }
//
//    private static DataSource buildDataSource() {
//
//        final HikariConfig config = new HikariConfig();
//
//        config.setJdbcUrl(System.getProperty("db.url"));
//        config.setUsername(System.getProperty("db.username"));
//        config.setPassword(System.getProperty("db.password"));
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//
//        return new HikariDataSource(config);
//
//    }
//
//    public static void main(String[] args) {
//        try {
//            final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer();
//            server.start();
//        } catch (Exception e) {
//            log.error("Kunne ikke starte opp sosialhjelp-soknad-api", e);
//        }
//    }
//}
