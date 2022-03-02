package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

class SlettLoggScheduler(
    private val leaderElection: LeaderElection,
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository,
    private val batchSendtSoknadRepository: BatchSendtSoknadRepository,
    private val oppgaveRepository: OppgaveRepository,
    private val batchEnabled: Boolean,
    private val schedulerDisabled: Boolean
) {
    private var batchStartTime: LocalDateTime? = null
    private var vellykket = 0

    @Scheduled(cron = KLOKKEN_FEM_OM_NATTEN)
    fun slettLogger() {
        if (schedulerDisabled) {
            logger.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            if (batchEnabled) {
                logger.info("Starter sletting av logger for ett år gamle søknader")
                val batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.slettLogg")
                batchTimer.start()
                try {
                    slettForeldetLogg()
                } catch (e: RuntimeException) {
                    logger.error("Batchjobb feilet for sletting av logg", e)
                    batchTimer.setFailed()
                } finally {
                    batchTimer.stop()
                    batchTimer.addFieldToReport("vellykket", vellykket)
                    batchTimer.report()
                    logger.info("Jobb fullført for sletting av logg: $vellykket vellykket")
                }
            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun slettForeldetLogg() {
        var soknad = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        while (soknad.isPresent) {
            val soknadMetadata = soknad.get()

            val behandlingsId = soknadMetadata.behandlingsId

            val sendtSoknadIdOptional = batchSendtSoknadRepository.hentSendtSoknad(behandlingsId)
            sendtSoknadIdOptional.ifPresent { batchSendtSoknadRepository.slettSendtSoknad(it) }

            val oppgaveOptional = oppgaveRepository.hentOppgave(behandlingsId)
            oppgaveOptional.ifPresent { oppgaveRepository.slettOppgave(behandlingsId) }

            batchSoknadMetadataRepository.slettSoknadMetaData(behandlingsId)

            vellykket++

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                return
            }
            soknad = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return LocalDateTime.now().isAfter(batchStartTime!!.plusSeconds(SCHEDULE_INTERRUPT_S))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SlettLoggScheduler::class.java)
        private const val KLOKKEN_FEM_OM_NATTEN = "0 0 5 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 30 // 30 min
        private const val DAGER_GAMMELT = 365 // Ett år
    }
}
