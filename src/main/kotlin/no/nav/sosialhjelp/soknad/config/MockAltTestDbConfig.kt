package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.business.db.SQLUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.util.Properties
import javax.sql.DataSource

@Profile("(mock-alt|test)")
@Configuration
open class MockAltTestDbConfig {

    @Bean
    open fun dataSource(): DataSource {
        return buildDataSource()
    }

    @Bean
    open fun transactionManager(): DataSourceTransactionManager {
        return DataSourceTransactionManager(dataSource())
    }

    @Bean
    open fun transactionTemplate(): TransactionTemplate? {
        return TransactionTemplate(transactionManager())
    }

    companion object {
        fun buildDataSource(): DataSource {
            return if (erInMemoryDatabase()) {
                buildDataSource("hsqldb.properties")
            } else {
                buildDataSource("oracledb.properties")
            }
        }

        private fun erInMemoryDatabase(): Boolean {
            val dbProp = System.getProperty("no.nav.sosialhjelp.soknad.hsqldb", "true")
            return dbProp == null || dbProp.equals("true", ignoreCase = true)
        }

        fun buildDataSource(propertyFileName: String): DataSource {
            val dataSource = DriverManagerDataSource()
            //
            // dataSource.setSuppressClose(true);
            val env = dbProperties(propertyFileName)
            dataSource.setDriverClassName(env.getProperty("db.driverClassName"))
            dataSource.url = env.getProperty("db.url")
            dataSource.username = env.getProperty("db.username")
            dataSource.password = env.getProperty("db.password")
            if (erInMemoryDatabase()) {
                System.setProperty(SQLUtils.DIALECT_PROPERTY, "hsqldb")
                createNonJpaTables(dataSource)
            }
            return dataSource
        }

        private fun dbProperties(propertyFileName: String): Properties {
            val env = Properties()
            env.load(this::class.java.getResourceAsStream("/$propertyFileName"))
            return env
        }

        private fun createNonJpaTables(dataSource: DataSource) {
            try {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { st ->
                        st.execute("drop table HENVENDELSE if exists")
                        st.execute(
                            "create table HENVENDELSE (henvendelse_id bigint, behandlingsid varchar(255), behandlingskjedeId varchar(255), traad varchar(255), type varchar(255), opprettetdato timestamp, " +
                                "lestdato timestamp, sistendretdato timestamp, tema varchar(255), aktor varchar(255), status varchar(255), behandlingsresultat varchar(2048), sensitiv integer)"
                        )
                        st.execute("drop table HENDELSE if exists")
                        st.execute("create table HENDELSE (BEHANDLINGSID varchar(255), HENDELSE_TYPE varchar(255), HENDELSE_TIDSPUNKT timestamp not null, VERSJON int, SKJEMANUMMER varchar(255), SIST_HENDELSE integer not null)")
                        st.execute("drop sequence BRUKERBEH_ID_SEQ if exists")
                        st.execute("create sequence BRUKERBEH_ID_SEQ as integer start with 1 increment by 1")
                        st.execute("drop table SOKNADBRUKERDATA if exists")
                        st.execute("drop table FAKTUMEGENSKAP if exists")
                        st.execute("drop table SOKNAD if exists")
                        st.execute(
                            "create table SOKNAD (soknad_id numeric not null, uuid varchar(255) not null, brukerbehandlingid varchar(255) not null, behandlingskjedeid varchar(255), navsoknadid varchar(255) not null, " +
                                "aktorid varchar(255) not null, opprettetdato timestamp not null, status varchar(255) not null, delstegstatus varchar(255), sistlagret timestamp, journalforendeEnhet varchar(255))"
                        )
                        st.execute("alter table SOKNAD add batch_status varchar(255) default 'LEDIG'")
                        st.execute("drop table VEDLEGG if exists")
                        st.execute(
                            "create table VEDLEGG (vedlegg_id bigint not null , soknad_id bigint not null, faktum bigint, skjemaNummer varchar(36), aarsak varchar(200), navn varchar(255) not null,innsendingsvalg varchar(255) not null , opprinneliginnsendingsvalg varchar(255), antallsider bigint, fillagerReferanse varchar(36), sha512 varchar(256), storrelse bigint not null, " +
                                " opprettetdato timestamp , data blob, mimetype varchar(200), filnavn varchar(200))"
                        )
                        st.execute(
                            "create table SOKNADBRUKERDATA (soknadbrukerdata_id bigint not null, soknad_id bigint not null, key varchar(255) not null, value varchar(2000), " +
                                "type varchar(255), sistendret timestamp not null, PARRENT_FAKTUM bigint)"
                        )
                        st.execute("create table FAKTUMEGENSKAP (soknad_id bigint not null,faktum_id bigint not null, key varchar(255) not null, value varchar(2000), systemegenskap bit) ")
                        st.execute("drop sequence SOKNAD_ID_SEQ if exists")
                        st.execute("create sequence SOKNAD_ID_SEQ as integer start with 1 increment by 1")
                        st.execute("drop sequence SOKNAD_BRUKER_DATA_ID_SEQ if exists")
                        st.execute("create sequence SOKNAD_BRUKER_DATA_ID_SEQ as integer start with 1 increment by 1")
                        st.execute("drop sequence VEDLEGG_ID_SEQ if exists")
                        st.execute("create sequence VEDLEGG_ID_SEQ as integer start with 1 increment by 1")
                        st.execute("drop table FILLAGER if exists")
                        st.execute("create table FILLAGER (behandlingsid varchar(255), uuid varchar(255), eier varchar(255), data blob)")
                        st.execute("drop sequence METADATA_ID_SEQ if exists")
                        st.execute("create sequence METADATA_ID_SEQ as integer start with 1 increment by 1")
                        st.execute("drop table SOKNADMETADATA if exists")
                        st.execute(
                            "create table SOKNADMETADATA (id numeric not null, behandlingsId varchar(255) not null, tilknyttetBehandlingsId varchar(255), skjema varchar(255), fnr varchar(255), " +
                                "hovedskjema clob, vedlegg clob, orgnr varchar(255), navenhet varchar(255), fiksforsendelseid varchar(255), soknadtype varchar(255), innsendingstatus varchar(255), " +
                                "opprettetdato timestamp, sistendretdato timestamp, innsendtdato timestamp, batchstatus varchar(255))"
                        )
                        st.execute("drop sequence OPPGAVE_ID_SEQ if exists")
                        st.execute("create sequence OPPGAVE_ID_SEQ as integer start with 1 increment by 1")
                        st.execute("drop table OPPGAVE if exists")
                        st.execute(
                            "create table OPPGAVE (id numeric not null, behandlingsid varchar(255), type varchar(255), status varchar(255), steg numeric, oppgavedata clob, " +
                                "oppgaveresultat clob, opprettet timestamp, sistkjort timestamp, nesteforsok timestamp, retries numeric)"
                        )
                        st.execute("drop table SENDT_SOKNAD if exists")
                        st.execute(
                            "CREATE TABLE SENDT_SOKNAD (SENDT_SOKNAD_ID bigint NOT NULL, BEHANDLINGSID varchar(255) NOT NULL, TILKNYTTETBEHANDLINGSID varchar(255), EIER varchar(255) NOT NULL," +
                                " FIKSFORSENDELSEID varchar(255), ORGNR VARCHAR(255) NOT NULL, NAVENHETSNAVN VARCHAR(255) NOT NULL, BRUKEROPPRETTETDATO TIMESTAMP(3) DEFAULT SYSDATE NOT NULL, BRUKERFERDIGDATO TIMESTAMP(3) DEFAULT SYSDATE NOT NULL, SENDTDATO TIMESTAMP(3) DEFAULT SYSDATE," +
                                " CONSTRAINT UNIK_SS_BEHANDLINGSID UNIQUE (BEHANDLINGSID), CONSTRAINT UNIK_SS_FIKSFORSENDELSEID UNIQUE (FIKSFORSENDELSEID), CONSTRAINT SENDT_SOKNAD_PK PRIMARY KEY (SENDT_SOKNAD_ID))"
                        )
                        st.execute("drop sequence SENDT_SOKNAD_ID_SEQ if exists ")
                        st.execute("CREATE sequence SENDT_SOKNAD_ID_SEQ start WITH 1 increment BY 1")
                        st.execute("drop table VEDLEGGSTATUS if exists ")
                        st.execute(
                            "CREATE TABLE VEDLEGGSTATUS(VEDLEGGSTATUS_ID bigint NOT NULL, EIER VARCHAR(255) NOT NULL, STATUS VARCHAR(255) NOT NULL, TYPE VARCHAR(255) NOT NULL," +
                                " SENDT_SOKNAD_ID bigint NOT NULL, CONSTRAINT UNIK_IDTYPE UNIQUE (SENDT_SOKNAD_ID, TYPE), CONSTRAINT VEDLEGGSTATUS_PK PRIMARY KEY (VEDLEGGSTATUS_ID))"
                        )
                        st.execute("drop sequence VEDLEGGSTATUSID_SEQ if exists ")
                        st.execute("CREATE sequence VEDLEGGSTATUSID_SEQ start WITH 1 increment BY 1")
                        st.execute("drop table SOKNAD_UNDER_ARBEID if exists")
                        st.execute(
                            "CREATE TABLE SOKNAD_UNDER_ARBEID (SOKNAD_UNDER_ARBEID_ID bigint NOT NULL, VERSJON bigint DEFAULT 1 NOT NULL, BEHANDLINGSID VARCHAR(255) NOT NULL, TILKNYTTETBEHANDLINGSID VARCHAR(255)," +
                                " EIER VARCHAR(255) NOT NULL, DATA BLOB, STATUS VARCHAR(255) NOT NULL, OPPRETTETDATO TIMESTAMP(3) DEFAULT SYSDATE NOT NULL, SISTENDRETDATO TIMESTAMP(3) DEFAULT SYSDATE NOT NULL," +
                                " CONSTRAINT UNIK_UA_BEHANDLINGSID UNIQUE (BEHANDLINGSID), CONSTRAINT SOKNAD_UNDER_ARBEID_PK PRIMARY KEY (SOKNAD_UNDER_ARBEID_ID))"
                        )
                        st.execute("drop sequence SOKNAD_UNDER_ARBEID_ID_SEQ if exists")
                        st.execute("CREATE sequence SOKNAD_UNDER_ARBEID_ID_SEQ start WITH 1 increment BY 1")
                        st.execute("drop table OPPLASTET_VEDLEGG if exists")
                        st.execute(
                            "CREATE TABLE OPPLASTET_VEDLEGG(UUID VARCHAR(255) NOT NULL, EIER VARCHAR(255) NOT NULL, TYPE VARCHAR(255) NOT NULL, DATA blob NOT NULL, SOKNAD_UNDER_ARBEID_ID bigint NOT NULL," +
                                " FILNAVN VARCHAR(255) NOT NULL, SHA512 VARCHAR(255) NOT NULL, CONSTRAINT UNIK_OPPLASTET_VEDLEGG_UUID UNIQUE (UUID))"
                        )
                        st.execute("drop table FAMKTUMEGENSKAP if exists")
                        st.execute("drop table FILLAGER if exists")
                        st.execute("drop table HENDELSE if exists")
                        st.execute("drop table SOKNADBRUKERDATA if exists")
                        st.execute("drop table SOKNAD if exists")
                        st.execute("drop table VEDLEGG if exists")
                        st.execute("drop table VEDLEGGSTATUS if exists")
                        st.execute("alter table SOKNADMETADATA add LEST_DITT_NAV BOOLEAN default FALSE NOT NULL")
                    }
                }
            } catch (e: SQLException) {
                throw RuntimeException("Feil ved oppretting av databasen", e)
            }
        }
    }
}
