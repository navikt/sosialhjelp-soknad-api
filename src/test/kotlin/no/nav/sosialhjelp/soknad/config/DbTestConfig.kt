package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.business.db.TestSupport
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
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource

@Configuration
@Import(value = [MockAltTestDbConfig::class])
@EnableTransactionManagement
open class DbTestConfig {

    @Bean
    open fun soknadMetadataRepository(): SoknadMetadataRepository {
        return SoknadMetadataRepositoryJdbc()
    }

    @Bean
    open fun sendtSoknadRepository(): SendtSoknadRepository {
        return SendtSoknadRepositoryJdbc()
    }

    @Bean
    open fun soknadUnderArbeidRepository(
        transactionTemplate: TransactionTemplate,
        opplastetVedleggRepository: OpplastetVedleggRepository
    ): SoknadUnderArbeidRepository {
        return SoknadUnderArbeidRepositoryJdbc(transactionTemplate, opplastetVedleggRepository)
    }

    @Bean
    open fun opplastetVedleggRepository(): OpplastetVedleggRepository {
        return OpplastetVedleggRepositoryJdbc()
    }

    @Bean
    open fun batchSoknadUnderArbeidRepository(
        transactionTemplate: TransactionTemplate,
        batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository
    ): BatchSoknadUnderArbeidRepository {
        return BatchSoknadUnderArbeidRepositoryJdbc(transactionTemplate, batchOpplastetVedleggRepository)
    }

    @Bean
    open fun batchOpplastetVedleggRepository(): BatchOpplastetVedleggRepository {
        return BatchOpplastetVedleggRepositoryJdbc()
    }

    @Bean
    open fun batchSoknadMetadataRepository(): BatchSoknadMetadataRepository {
        return BatchSoknadMetadataRepositoryJdbc()
    }

    @Bean
    open fun batchSendtSoknadRepository(): BatchSendtSoknadRepository {
        return BatchSendtSoknadRepositoryJdbc()
    }

    @Bean
    open fun testSupport(dataSource: DataSource): RepositoryTestSupport {
        return TestSupport(dataSource)
    }
}
