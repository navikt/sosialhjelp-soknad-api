package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SchedulerConfig(
    @Value("\${sendsoknad.batch.enabled}") private val batchEnabled: Boolean,
    private val leaderElection: LeaderElection,
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val batchSendtSoknadRepository: BatchSendtSoknadRepository,
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository,
    private val batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository,
    private val henvendelseService: HenvendelseService
) {

    @Bean
    open fun avbrytAutomatiskScheduler(): AvbrytAutomatiskScheduler {
        return AvbrytAutomatiskScheduler(
            leaderElection,
            soknadMetadataRepository,
            batchSoknadMetadataRepository,
            batchSoknadUnderArbeidRepository,
            batchEnabled
        )
    }

    @Bean
    open fun lagringsScheduler(): LagringsScheduler {
        return LagringsScheduler(
            leaderElection, henvendelseService, batchSoknadUnderArbeidRepository, batchEnabled
        )
    }
}
