package no.nav.sbl.dialogarena.soknadinnsending.business.db.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static java.lang.System.getProperty;

@Configuration
public class DatabaseTestContext {


    @Bean
    public DataSource dataSource() throws IOException {
        return buildDataSource();
    }
    @Bean
    public DataSourceTransactionManager transactionManager() throws IOException {
        return new DataSourceTransactionManager(dataSource());
    }

    public static DataSource buildDataSource() throws IOException {
        if (erInMemoryDatabase()) {
            return buildDataSource("hsqldb.properties");
        } else {
            return buildDataSource("oracledb.properties");
        }
    }

    private static boolean erInMemoryDatabase() {
        String dbProp = getProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");
        if (dbProp == null) {
            return true;
        }
        return dbProp.equalsIgnoreCase("true");
    }

    public static SingleConnectionDataSource buildDataSource(String propertyFileName) throws IOException {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setSuppressClose(true);
        Properties env = dbProperties(propertyFileName);
        dataSource.setDriverClassName(env.getProperty("db.driverClassName"));
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));
        if (erInMemoryDatabase()) {
            System.setProperty(SQLUtils.DIALECT_PROPERTY, "hsqldb");
            createNonJpaTables(dataSource);
        }
        return dataSource;
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
            st.execute("drop sequence BRUKERBEH_ID_SEQ if exists");
            st.execute("create sequence BRUKERBEH_ID_SEQ as integer start with 1 increment by 1");
            st.execute("drop table SOKNADBRUKERDATA if exists");
            st.execute("drop table SOKNAD if exists");
            st.execute("create table SOKNAD (soknad_id numeric not null, brukerbehandlingid varchar(255) not null, navsoknadid varchar(255) not null, " +
                    "aktorid varchar(255) not null, opprettetdato timestamp not null, status varchar(255) not null, delstegstatus varchar(255))");
            st.execute("drop table VEDLEGG if exists");
            st.execute("create table VEDLEGG (vedlegg_id bigint not null , soknad_id bigint not null, faktum bigint not null, navn varchar(255) not null, storrelse bigint not null, " +
                    " opprettetdato timestamp , data blob)");
            st.execute("create table SOKNADBRUKERDATA (soknadbrukerdata_id bigint not null, soknad_id bigint not null, key varchar(255) not null, value varchar(2000) not null, " +
                    "type varchar(255), sistendret timestamp not null, vedlegg_id bigint)");
            st.execute("drop sequence SOKNAD_ID_SEQ if exists");
            st.execute("create sequence SOKNAD_ID_SEQ as integer start with 1 increment by 1");
            st.execute("drop sequence SOKNAD_BRUKER_DATA_ID_SEQ if exists");
            st.execute("create sequence SOKNAD_BRUKER_DATA_ID_SEQ as integer start with 1 increment by 1");
            st.execute("drop sequence VEDLEGG_ID_SEQ if exists");
            st.execute("create sequence VEDLEGG_ID_SEQ as integer start with 1 increment by 1");
        } catch (SQLException e) {
            throw new RuntimeException("Feil ved oppretting av databasen", e);
        }
    }

}
