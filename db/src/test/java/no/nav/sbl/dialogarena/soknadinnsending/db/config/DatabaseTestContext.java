package no.nav.sbl.dialogarena.soknadinnsending.db.config;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import no.nav.sbl.dialogarena.soknadinnsending.db.SQLUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

@Configuration
public class DatabaseTestContext {

    @Bean
    public SimpleNamingContextBuilder setupJndiResources() throws IOException, NamingException {
        SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.getCurrentContextBuilder();
        if (null == builder) {
            builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        }
        builder.bind("java:jboss/datasources/SoknadInnsendingDS", buildDataSource());
        return builder;
    }

    @Bean
    public DataSource dataSource(SimpleNamingContextBuilder namingcontextbuilder) throws NamingException {
        // Tvinger lasting av denne bønnen før vi prøver å slå opp
        namingcontextbuilder.getClass();
        InitialContext ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:jboss/datasources/SoknadInnsendingDS");
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    
    public static SingleConnectionDataSource buildDataSource() throws IOException {
        return buildDataSource("hsqldb.properties");
    }

    public static SingleConnectionDataSource buildDataSource(String propertyFileName) throws IOException {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setSuppressClose(true);
        Properties env = dbProperties(propertyFileName);
        dataSource.setDriverClassName(env.getProperty("db.driverClassName"));
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));
        if (Boolean.parseBoolean(env.getProperty("db.erHsqldb", "true"))) {
            System.setProperty(SQLUtils.DIALECT_PROPERTY, "hsqldb");
            createNonJpaTables(dataSource);
        }
        return dataSource;
    }

    private static Properties dbProperties() throws IOException {
        return dbProperties("hsqldb.properties");
    }

    private static Properties dbProperties(String propertyFileName) throws IOException {
        Properties env = new Properties();
        env.load(DatabaseTestContext.class.getResourceAsStream("/" + propertyFileName));
        return env;
    }
    
    private static void createNonJpaTables(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("drop table HENVENDELSE if exists");
            st.execute("create table HENVENDELSE (henvendelse_id bigint, behandlingsid varchar(255), behandlingskjedeId varchar(255), traad varchar(255), type varchar(255), opprettetdato timestamp, " +
            		"lestdato timestamp, sistendretdato timestamp, tema varchar(255), aktor varchar(255), status varchar(255), behandlingsresultat varchar(2048), sensitiv integer)");
            st.execute("drop table SOKNADBRUKERDATA if exists");
            st.execute("drop table SOKNAD if exists");
            st.execute("create table SOKNAD (soknad_id numeric not null, brukerbehandlingid varchar(255) not null, navsoknadid varchar(255) not null, " +
                    "aktorid varchar(255) not null, opprettetdato timestamp not null, status varchar(255) not null)");
            st.execute("create table SOKNADBRUKERDATA (soknadbrukerdata_id numeric not null, soknad_id numeric not null, key varchar(255) not null, value varchar(255) not null, " +
                    "type varchar(255), sistendret timestamp not null)");
            st.execute("drop sequence SOKNAD_ID_SEQ if exists");
            st.execute("create sequence SOKNAD_ID_SEQ as integer start with 1 increment by 1");
            st.execute("drop sequence SOKNAD_BRUKER_DATA_ID_SEQ if exists");
            st.execute("create sequence SOKNAD_BRUKER_DATA_ID_SEQ as integer start with 1 increment by 1");
        } catch (SQLException e) {
            throw new RuntimeException("Feil ved oppretting av databasen", e);
        }
    }

}
