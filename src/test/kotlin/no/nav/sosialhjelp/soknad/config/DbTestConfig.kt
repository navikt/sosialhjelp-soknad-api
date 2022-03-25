package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepositoryJdbc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.util.Properties
import javax.sql.DataSource

@Configuration
@Profile("repositoryTest")
@EnableTransactionManagement
open class DbTestConfig {

    @Bean
    open fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Bean
    open fun soknadMetadataRepository(jdbcTemplate: JdbcTemplate): SoknadMetadataRepository {
        return SoknadMetadataRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    open fun sendtSoknadRepository(jdbcTemplate: JdbcTemplate): SendtSoknadRepository {
        return SendtSoknadRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    open fun soknadUnderArbeidRepository(
        jdbcTemplate: JdbcTemplate,
        transactionTemplate: TransactionTemplate,
        opplastetVedleggRepository: OpplastetVedleggRepository
    ): SoknadUnderArbeidRepository {
        return SoknadUnderArbeidRepositoryJdbc(jdbcTemplate, transactionTemplate, opplastetVedleggRepository)
    }

    @Bean
    open fun opplastetVedleggRepository(jdbcTemplate: JdbcTemplate): OpplastetVedleggRepository {
        return OpplastetVedleggRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    open fun batchSoknadUnderArbeidRepository(
        jdbcTemplate: JdbcTemplate,
        transactionTemplate: TransactionTemplate,
        batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository
    ): BatchSoknadUnderArbeidRepository {
        return BatchSoknadUnderArbeidRepositoryJdbc(jdbcTemplate, transactionTemplate, batchOpplastetVedleggRepository)
    }

    @Bean
    open fun batchOpplastetVedleggRepository(jdbcTemplate: JdbcTemplate): BatchOpplastetVedleggRepository {
        return BatchOpplastetVedleggRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    open fun batchSoknadMetadataRepository(jdbcTemplate: JdbcTemplate): BatchSoknadMetadataRepository {
        return BatchSoknadMetadataRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    open fun batchSendtSoknadRepository(jdbcTemplate: JdbcTemplate, transactionTemplate: TransactionTemplate): BatchSendtSoknadRepository {
        return BatchSendtSoknadRepositoryJdbc(jdbcTemplate, transactionTemplate)
    }

    @Bean
    open fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        // dataSource.setSuppressClose(true);
        val env = dbProperties("hsqldb.properties")
        dataSource.setDriverClassName(env.getProperty("db.driverClassName"))
        dataSource.url = env.getProperty("db.url")
        dataSource.username = env.getProperty("db.username")
        dataSource.password = env.getProperty("db.password")
        System.setProperty(SQLUtils.DIALECT_PROPERTY, "hsqldb")
        createNonJpaTables(dataSource)
        return dataSource
    }

    @Bean
    open fun transactionManager(dataSource: DataSource): DataSourceTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    open fun transactionTemplate(transactionManager: DataSourceTransactionManager): TransactionTemplate? {
        return TransactionTemplate(transactionManager)
    }

    companion object {
        private fun dbProperties(propertyFileName: String): Properties {
            val env = Properties()
            env.load(this::class.java.getResourceAsStream("/$propertyFileName"))
            return env
        }

        private fun createNonJpaTables(dataSource: DataSource) {
            try {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { st ->
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
                        st.execute("alter table SOKNADMETADATA add LEST_DITT_NAV BOOLEAN default FALSE NOT NULL")
                    }
                }
            } catch (e: SQLException) {
                throw RuntimeException("Feil ved oppretting av databasen", e)
            }
        }
    }
}
