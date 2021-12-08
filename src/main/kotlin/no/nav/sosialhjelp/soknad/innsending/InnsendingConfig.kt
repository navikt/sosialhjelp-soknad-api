package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.support.TransactionTemplate

@Configuration
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
