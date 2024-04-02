package no.nav.sosialhjelp.soknad.db

import no.nav.sosialhjelp.soknad.app.config.MockAltTestDbConfig
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
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource

@Configuration
@Import(value = [MockAltTestDbConfig::class])
@EnableTransactionManagement
class DbTestConfig {

    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    fun soknadMetadataRepository(jdbcTemplate: JdbcTemplate): SoknadMetadataRepository {
        return SoknadMetadataRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    fun soknadUnderArbeidRepository(
        jdbcTemplate: JdbcTemplate,
        transactionTemplate: TransactionTemplate,
    ): SoknadUnderArbeidRepository {
        return SoknadUnderArbeidRepositoryJdbc(jdbcTemplate, transactionTemplate)
    }

    @Bean
    fun batchSoknadUnderArbeidRepository(
        jdbcTemplate: JdbcTemplate,
        transactionTemplate: TransactionTemplate,
    ): BatchSoknadUnderArbeidRepository {
        return BatchSoknadUnderArbeidRepositoryJdbc(jdbcTemplate, transactionTemplate)
    }

    @Bean
    fun batchSoknadMetadataRepository(jdbcTemplate: JdbcTemplate, namedParameterJdbcTemplate: NamedParameterJdbcTemplate): BatchSoknadMetadataRepository {
        return BatchSoknadMetadataRepositoryJdbc(jdbcTemplate, namedParameterJdbcTemplate)
    }
}
