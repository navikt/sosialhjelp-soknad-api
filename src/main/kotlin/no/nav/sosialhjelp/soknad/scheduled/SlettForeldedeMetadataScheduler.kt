package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
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
    private var iterasjoner = 0

    @Scheduled(cron = KLOKKEN_05_OM_NATTEN)
    fun slettForeldedeMetadata() {
        if (schedulerDisabled) {
            log.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            iterasjoner = 0
            if (batchEnabled) {
                log.info("Starter sletting av metadata for ett år gamle søknader")
                try {
                    slett()
                } catch (e: RuntimeException) {
                    log.error("Batchjobb feilet for sletting av logg", e)
                } finally {
                    log.info("Jobb fullført for sletting av metadata: $vellykket vellykket. Iterasjoner = $iterasjoner")
                }
            } else {
                log.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun slett() {
        var soknadMetadataList = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        while (soknadMetadataList.isNotEmpty()) {
            val behandlingsIdList = soknadMetadataList.map { it.behandlingsId }

            val sendtSoknadIdList = batchSendtSoknadRepository.hentSendtSoknadIdList(behandlingsIdList)
            if (sendtSoknadIdList.isNotEmpty()) batchSendtSoknadRepository.slettSendtSoknader(sendtSoknadIdList)

            val oppgaver = oppgaveRepository.hentOppgaveIdList(behandlingsIdList)
            if (oppgaver.isNotEmpty()) oppgaveRepository.slettOppgaver(oppgaver)

            batchSoknadMetadataRepository.slettSoknadMetaDataer(behandlingsIdList)

            vellykket += soknadMetadataList.size
            iterasjoner++

            if (harGaattForLangTid()) {
                log.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                return
            }
            soknadMetadataList = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT)
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return batchStartTime
            ?.let { LocalDateTime.now().isAfter(it.plusSeconds(SCHEDULE_INTERRUPT_S)) }
            ?: true.also { log.warn("SlettForeldedeMetadataScheduler finner ikke batchStartTime - avbryter batchjobben") }
    }

    companion object {
        private val log by logger()
        private const val KLOKKEN_05_OM_NATTEN = "0 0 5 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 10 // 10 min
        private const val DAGER_GAMMELT = 365 // Ett år
    }
}
