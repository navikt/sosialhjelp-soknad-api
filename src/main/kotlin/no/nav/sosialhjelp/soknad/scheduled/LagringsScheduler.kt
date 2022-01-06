package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.metrics.Timer
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class LagringsScheduler(
    private val leaderElection: LeaderElection,
    private val henvendelseService: HenvendelseService,
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository,
    private val batchEnabled: Boolean
) {
    private var batchStartTime: ZonedDateTime? = null
    private var vellykket = 0
    private var feilet = 0

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    fun slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = ZonedDateTime.now()
            vellykket = 0
            feilet = 0
            if (batchEnabled) {
                logger.info("Starter flytting av søknader til henvendelse-jobb")
                val batchTimer = MetricsFactory.createTimer("debug.lagringsjobb")
                batchTimer.start()

                hentForeldedeEttersendelserFraDatabaseOgSlett(batchTimer)

                batchTimer.stop()
                batchTimer.addFieldToReport("vellykket", vellykket)
                batchTimer.addFieldToReport("feilet", feilet)
                batchTimer.report()
                logger.info("Jobb fullført: $vellykket vellykket, $feilet feilet")
            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen")
            }
        }
    }

    private fun hentForeldedeEttersendelserFraDatabaseOgSlett(metrikk: Timer) {
        val soknadUnderArbeidList = batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser()
        for (soknadUnderArbeid in soknadUnderArbeidList) {
            if (soknadUnderArbeid.erEttersendelse()) {
                avbrytOgSlettEttersendelse(soknadUnderArbeid)

                // Avslutt prosessen hvis det er gått for lang tid. Tyder på at noe er nede.
                if (harGaattForLangTid()) {
                    logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_MS ms. Den blir derfor stoppet")
                    metrikk.addFieldToReport("avbruttPgaTid", true)
                    return
                }
            } else {
                logger.warn("hentForeldedeEttersendelser har returnet soknadUnderArbeid som ikke er ettersendelse")
            }
        }
    }

    private fun avbrytOgSlettEttersendelse(soknadUnderArbeid: SoknadUnderArbeid) {
        try {
            henvendelseService.avbrytSoknad(soknadUnderArbeid.behandlingsId, true)
            batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid.soknadId)

            vellykket++
        } catch (e: Exception) {
            feilet++
            logger.error("Avbryt feilet for ettersending ${soknadUnderArbeid.soknadId}.", e)
            Thread.sleep(1000) // Så loggen ikke blir fylt opp
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return ZonedDateTime.now().isAfter(batchStartTime!!.plus(SCHEDULE_INTERRUPT_MS, ChronoUnit.MILLIS))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LagringsScheduler::class.java)
        private const val SCHEDULE_RATE_MS: Long = 1000 * 60 * 60 // 1 time
        private const val SCHEDULE_INTERRUPT_MS: Long = 1000 * 60 * 10 // 10 min
    }
}
