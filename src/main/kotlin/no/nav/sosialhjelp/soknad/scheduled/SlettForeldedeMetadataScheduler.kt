package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.repository.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.BatchSoknadMetadataRepository
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
    private val oppgaveRepository: OppgaveRepository,
    @Value("\${sendsoknad.batch.enabled}") private val batchEnabled: Boolean,
    @Value("\${scheduler.disable}") private val schedulerDisabled: Boolean,
) {
    private var batchStartTime: LocalDateTime? = null
    private var vellykket = 0
    private var iterasjoner = 0

    @Scheduled(cron = KLOKKEN_05_OM_NATTEN)
    fun slettForeldedeMetadata() {
        if (schedulerDisabled) {
            logger.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            iterasjoner = 0
            if (batchEnabled) {
                logger.info("Starter sletting av metadata for ett år gamle søknader")
                try {
                    slett()
                } catch (e: RuntimeException) {
                    logger.error("Batchjobb feilet for sletting av logg", e)
                } finally {
                    logger.info("Jobb fullført for sletting av metadata: $vellykket vellykket. Iterasjoner = $iterasjoner")
                }
            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun slett() {
        var soknadMetadataList = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        while (soknadMetadataList.isNotEmpty()) {
            val behandlingsIdList = soknadMetadataList.map { it.behandlingsId }

            val oppgaver = oppgaveRepository.hentOppgaveIdList(behandlingsIdList)
            if (oppgaver.isNotEmpty()) oppgaveRepository.slettOppgaver(oppgaver)

            batchSoknadMetadataRepository.slettSoknadMetaDataer(behandlingsIdList)

            vellykket += soknadMetadataList.size
            iterasjoner++

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                return
            }
            soknadMetadataList = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return batchStartTime
            ?.let { LocalDateTime.now().isAfter(it.plusSeconds(SCHEDULE_INTERRUPT_S)) }
            ?: true.also { logger.warn("SlettForeldedeMetadataScheduler finner ikke batchStartTime - avbryter batchjobben") }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SlettForeldedeMetadataScheduler::class.java)
        private const val KLOKKEN_05_OM_NATTEN = "0 0 5 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 10 // 10 min
        private const val DAGER_GAMMELT = 365 // Ett år
    }
}
