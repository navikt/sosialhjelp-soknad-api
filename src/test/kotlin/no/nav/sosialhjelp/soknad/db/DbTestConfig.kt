package no.nav.sosialhjelp.soknad.db

import no.nav.sosialhjelp.soknad.app.config.MockAltTestDbConfig
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepositoryJdbc
import no.nav.sosialhjelp.soknad.migration.repo.OpplastetVedleggMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadMetadataMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadUnderArbeidMigrationRepository
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
    fun soknadMetadataMigrationRepository(jdbcTemplate: JdbcTemplate): SoknadMetadataMigrationRepository {
        return SoknadMetadataMigrationRepository(jdbcTemplate)
    }

    @Bean
    fun soknadUnderArbeidRepository(
        jdbcTemplate: JdbcTemplate,
        transactionTemplate: TransactionTemplate,
        opplastetVedleggRepository: OpplastetVedleggRepository
    ): SoknadUnderArbeidRepository {
        return SoknadUnderArbeidRepositoryJdbc(jdbcTemplate, transactionTemplate, opplastetVedleggRepository)
    }

    @Bean
    fun soknadUnderArbeidMigrationRepository(jdbcTemplate: JdbcTemplate): SoknadUnderArbeidMigrationRepository {
        return SoknadUnderArbeidMigrationRepository(jdbcTemplate)
    }

    @Bean
    fun opplastetVedleggRepository(jdbcTemplate: JdbcTemplate): OpplastetVedleggRepository {
        return OpplastetVedleggRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    fun opplastetVedleggMigrationRepository(jdbcTemplate: JdbcTemplate): OpplastetVedleggMigrationRepository {
        return OpplastetVedleggMigrationRepository(jdbcTemplate)
    }

    @Bean
    fun batchSoknadUnderArbeidRepository(
        jdbcTemplate: JdbcTemplate,
        transactionTemplate: TransactionTemplate,
        batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository
    ): BatchSoknadUnderArbeidRepository {
        return BatchSoknadUnderArbeidRepositoryJdbc(jdbcTemplate, transactionTemplate, batchOpplastetVedleggRepository)
    }

    @Bean
    fun batchOpplastetVedleggRepository(jdbcTemplate: JdbcTemplate): BatchOpplastetVedleggRepository {
        return BatchOpplastetVedleggRepositoryJdbc(jdbcTemplate)
    }

    @Bean
    fun batchSoknadMetadataRepository(jdbcTemplate: JdbcTemplate, namedParameterJdbcTemplate: NamedParameterJdbcTemplate): BatchSoknadMetadataRepository {
        return BatchSoknadMetadataRepositoryJdbc(jdbcTemplate, namedParameterJdbcTemplate)
    }
}
