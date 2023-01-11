package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SlettForeldedeEttersendelserScheduler(
    private val leaderElection: LeaderElection,
    private val soknadService: SoknadService,
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository,
    @Value("\${sendsoknad.batch.enabled}") private val batchEnabled: Boolean,
    @Value("\${scheduler.disable}") private val schedulerDisabled: Boolean,
) {
    private var batchStartTime: LocalDateTime? = null
    private var vellykket = 0
    private var feilet = 0

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    fun slettForeldedeEttersendelser() {
        if (schedulerDisabled) {
            log.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            feilet = 0
            if (batchEnabled) {
                log.info("Starter sletting av foreldede ettersendelser fra SoknadUnderArbeid-tabell")

                hentForeldedeEttersendelserFraDatabaseOgSlett()

                log.info("Jobb fullført: $vellykket vellykket, $feilet feilet")
            } else {
                log.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun hentForeldedeEttersendelserFraDatabaseOgSlett() {
        val soknadUnderArbeidList = batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser()
        for (soknadUnderArbeid in soknadUnderArbeidList) {
            if (soknadUnderArbeid.erEttersendelse) {
                avbrytOgSlettEttersendelse(soknadUnderArbeid)

                // Avslutt prosessen hvis det er gått for lang tid. Tyder på at noe er nede.
                if (harGaattForLangTid()) {
                    log.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                    return
                }
            } else {
                log.warn("hentForeldedeEttersendelser har returnet soknadUnderArbeid som ikke er ettersendelse")
            }
        }
    }

    private fun avbrytOgSlettEttersendelse(soknadUnderArbeid: SoknadUnderArbeid) {
        try {
            soknadService.settSoknadMetadataAvbrutt(soknadUnderArbeid.behandlingsId, true)
            batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid.soknadId)

            vellykket++
        } catch (e: Exception) {
            feilet++
            log.error("Avbryt feilet for ettersending ${soknadUnderArbeid.soknadId}.", e)
            Thread.sleep(1000) // Så loggen ikke blir fylt opp
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return batchStartTime
            ?.let { LocalDateTime.now().isAfter(it.plusSeconds(SCHEDULE_INTERRUPT_S)) }
            ?: true.also { log.warn("SlettForeldedeEttersendelserScheduler finner ikke batchStartTime - avbryter batchjobben") }
    }

    companion object {
        private val log by logger()
        private const val SCHEDULE_RATE_MS: Long = 1000 * 60 * 60 // 1 time
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 10 // 10 min
    }
}
