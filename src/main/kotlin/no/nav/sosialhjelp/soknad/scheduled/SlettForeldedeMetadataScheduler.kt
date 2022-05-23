package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SlettForeldedeMetadataScheduler(
    private val leaderElection: LeaderElection,
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository,
    private val batchSendtSoknadRepository: BatchSendtSoknadRepository,
    private val oppgaveRepository: OppgaveRepository,
    @Value("\${sendsoknad.batch.enabled}") private val batchEnabled: Boolean,
    @Value("\${scheduler.disable}") private val schedulerDisabled: Boolean,
) {
    private var batchStartTime: LocalDateTime? = null
    private var vellykket = 0

    @Scheduled(cron = KLOKKEN_00_01_02_03_05_06_OM_NATTEN)
    fun slettForeldedeMetadata() {
        if (schedulerDisabled) {
            logger.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            if (batchEnabled) {
                logger.info("Starter sletting av metadata for ett år gamle søknader")
                val batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.slettLogg")
                batchTimer.start()
                try {
                    slett()
                } catch (e: RuntimeException) {
                    logger.error("Batchjobb feilet for sletting av logg", e)
                    batchTimer.setFailed()
                } finally {
                    batchTimer.stop()
                    batchTimer.addFieldToReport("vellykket", vellykket)
                    batchTimer.report()
                    logger.info("Jobb fullført for sletting av metadata: $vellykket vellykket")
                }
            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun slett() {
        var soknadMetadata = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        while (soknadMetadata != null) {
            val behandlingsId = soknadMetadata.behandlingsId

            batchSendtSoknadRepository.hentSendtSoknad(behandlingsId)
                ?.let { batchSendtSoknadRepository.slettSendtSoknad(it) }

            oppgaveRepository.hentOppgave(behandlingsId)
                ?.let { oppgaveRepository.slettOppgave(behandlingsId) }

            batchSoknadMetadataRepository.slettSoknadMetaData(behandlingsId)

            vellykket++

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                return
            }
            soknadMetadata = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return batchStartTime
            ?.let { LocalDateTime.now().isAfter(it.plusSeconds(SCHEDULE_INTERRUPT_S)) }
            ?: true.also { logger.warn("SlettForeldedeMetadataScheduler finner ikke batchStartTime - avbryter batchjobben") }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SlettForeldedeMetadataScheduler::class.java)
        private const val KLOKKEN_00_01_02_03_05_06_OM_NATTEN = "0 0 0,1,2,3,5,6 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 30 // 30 min
        private const val DAGER_GAMMELT = 365 // Ett år
    }
}
