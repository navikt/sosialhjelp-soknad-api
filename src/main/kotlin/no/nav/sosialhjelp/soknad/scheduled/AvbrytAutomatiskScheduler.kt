package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.repository.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AvbrytAutomatiskScheduler(
    @Value("\${sendsoknad.batch.enabled}") private val batchEnabled: Boolean,
    @Value("\${scheduler.disable}") private val schedulerDisabled: Boolean,
    private val leaderElection: LeaderElection,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository,
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository,
    private val mellomlagringService: MellomlagringService,
) {
    private var batchStartTime: LocalDateTime? = null
    private var vellykket = 0

    @Scheduled(cron = KLOKKEN_FIRE_OM_NATTEN)
    fun avbrytGamleSoknader() {
        if (schedulerDisabled) {
            logger.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            if (batchEnabled) {
                logger.info("Starter avbryting av gamle søknader")

                try {
                    avbrytSoknader()
                } catch (e: RuntimeException) {
                    logger.error("Batchjobb feilet", e)
                } finally {
                    logger.info("Jobb fullført: $vellykket vellykket")
                }
            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun avbrytSoknader() {
        var soknadMetadata = batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMELT)

        while (soknadMetadata != null) {
            soknadMetadata.status = AVBRUTT_AUTOMATISK
            soknadMetadata.sistEndretDato = LocalDateTime.now()
            soknadMetadataRepository.oppdater(soknadMetadata)

            val behandlingsId = soknadMetadata.behandlingsId

            batchSoknadUnderArbeidRepository.hentSoknadUnderArbeid(behandlingsId)?.let {
                if (mellomlagringService.kanSoknadHaMellomlagredeVedleggForSletting(it)) {
                    mellomlagringService.deleteAllVedlegg(behandlingsId)
                }
                batchSoknadUnderArbeidRepository.slettSoknad(it.soknadId)
            }

            batchSoknadMetadataRepository.leggTilbakeBatch(soknadMetadata.id)
            vellykket++

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                return
            }
            soknadMetadata = batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMELT)
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return batchStartTime
            ?.let { LocalDateTime.now().isAfter(it.plusSeconds(SCHEDULE_INTERRUPT_S)) }
            ?: true.also { logger.warn("AvbrytAutomatiskScheduler finner ikke batchStartTime - avbryter batchjobben") }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AvbrytAutomatiskScheduler::class.java)

        private const val KLOKKEN_FIRE_OM_NATTEN = "0 0 4 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 10
        private const val DAGER_GAMMELT = 7 * 2
    }
}
