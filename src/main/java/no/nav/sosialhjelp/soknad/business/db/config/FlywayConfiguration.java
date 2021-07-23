//package no.nav.sosialhjelp.soknad.business.db.config;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.flywaydb.core.Flyway;
//import org.flywaydb.core.api.output.MigrateResult;
//import org.slf4j.Logger;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//import javax.inject.Inject;
//import javax.sql.DataSource;
//
//import static org.slf4j.LoggerFactory.getLogger;
//
//@Configuration
//@Profile("!(mock-alt|local|test)")
//public class FlywayConfiguration {
//
//    private static final Logger log = getLogger(FlywayConfiguration.class);
//
//
//    private final DataSource dataSource;
//
//    public FlywayConfiguration(DataSource dataSource) {
//        this.dataSource = dataSource;
//        runMigration(dataSource);
//    }
//
//    private DataSource buildDataSource() {
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
//    }
//
//    public void runMigration(DataSource dataSource) {
//        log.debug("Running Flyway migration.");
//        final Flyway flyway = Flyway.configure().dataSource(dataSource).load();
//        MigrateResult migrateResult = flyway.migrate();
//        log.info("Flyway migration successfully executed. Number of new applied migrations: {}", migrateResult.migrationsExecuted);
//    }
//}
