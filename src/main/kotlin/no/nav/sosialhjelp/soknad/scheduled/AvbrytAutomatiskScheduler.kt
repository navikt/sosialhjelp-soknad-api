package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

class AvbrytAutomatiskScheduler(
    private val leaderElection: LeaderElection,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository,
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository,
    private val batchEnabled: Boolean,
    private val schedulerDisabled: Boolean
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
                val batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.avbryt")
                batchTimer.start()
                try {
                    avbrytSoknader()
                } catch (e: RuntimeException) {
                    logger.error("Batchjobb feilet", e)
                    batchTimer.setFailed()
                } finally {
                    batchTimer.stop()
                    batchTimer.addFieldToReport("vellykket", vellykket)
                    batchTimer.report()
                    logger.info("Jobb fullført: $vellykket vellykket")
                }
            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun avbrytSoknader() {
        var soknad = batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMELT)

        while (soknad.isPresent) {
            val soknadMetadata = soknad.get()
            soknadMetadata.status = AVBRUTT_AUTOMATISK
            soknadMetadata.sistEndretDato = LocalDateTime.now()
            soknadMetadataRepository.oppdater(soknadMetadata)

            val behandlingsId = soknadMetadata.behandlingsId

            val soknadUnderArbeidOptional = batchSoknadUnderArbeidRepository.hentSoknadUnderArbeidIdFromBehandlingsIdOptional(behandlingsId)
            soknadUnderArbeidOptional.ifPresent { batchSoknadUnderArbeidRepository.slettSoknad(it) }

            batchSoknadMetadataRepository.leggTilbakeBatch(soknadMetadata.id)
            vellykket++

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet",)
                return
            }
            soknad = batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMELT)
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return LocalDateTime.now().isAfter(batchStartTime!!.plusSeconds(SCHEDULE_INTERRUPT_S))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AvbrytAutomatiskScheduler::class.java)

        private const val KLOKKEN_FIRE_OM_NATTEN = "0 0 4 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 10
        private const val DAGER_GAMMELT = 7 * 2
    }
}
