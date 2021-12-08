package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidConfig
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.support.TransactionTemplate

@Configuration
@Import(
    SoknadUnderArbeidConfig::class
)
open class InnsendingConfig {

    @Bean
    open fun innsendingService(
        transactionTemplate: TransactionTemplate,
        sendtSoknadRepository: SendtSoknadRepository,
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
        opplastetVedleggRepository: OpplastetVedleggRepository,
        soknadUnderArbeidService: SoknadUnderArbeidService,
        soknadMetadataRepository: SoknadMetadataRepository
    ): InnsendingService {
        return InnsendingService(
            transactionTemplate,
            sendtSoknadRepository,
            soknadUnderArbeidRepository,
            opplastetVedleggRepository,
            soknadUnderArbeidService,
            soknadMetadataRepository
        )
    }
}
