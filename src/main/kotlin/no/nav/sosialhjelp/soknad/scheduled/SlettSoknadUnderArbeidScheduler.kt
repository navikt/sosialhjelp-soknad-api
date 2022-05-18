package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SlettSoknadUnderArbeidScheduler(
    @Value("\${sendsoknad.batch.enabled}") private val batchEnabled: Boolean,
    @Value("\${scheduler.disable}") private val schedulerDisabled: Boolean,
    private val leaderElection: LeaderElection,
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository,
    private val mellomlagringService: MellomlagringService,
) {
    private var batchStartTime: LocalDateTime? = null
    private var vellykket = 0

    @Scheduled(cron = KLOKKEN_HALV_FEM_OM_NATTEN)
    fun slettGamleSoknadUnderArbeid() {
        if (schedulerDisabled) {
            logger.warn("Scheduler is disabled")
            return
        }
        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now()
            vellykket = 0
            if (batchEnabled) {
                logger.info("Starter sletting av soknadUnderArbeid som er eldre enn 14 dager")
                val batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.slettSoknadUnderArbeid")
                batchTimer.start()
                try {
                    slett()
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

    private fun slett() {
        val soknadList = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch()
        soknadList.forEach { soknadUnderArbeid ->
            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn $SCHEDULE_INTERRUPT_S s. Den blir derfor stoppet")
                return
            }
            if (mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)) {
                mellomlagringService.deleteAllVedlegg(soknadUnderArbeid.behandlingsId)
            }
            batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid.soknadId)
            vellykket++
        }
    }

    private fun harGaattForLangTid(): Boolean {
        return batchStartTime
            ?.let { LocalDateTime.now().isAfter(it.plusSeconds(SCHEDULE_INTERRUPT_S)) }
            ?: true.also { logger.warn("SlettSoknadUnderArbeidsScheduler finner ikke batchStartTime - avbryter batchjobben") }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SlettSoknadUnderArbeidScheduler::class.java)
        private const val KLOKKEN_HALV_FEM_OM_NATTEN = "0 30 4 * * *"
        private const val SCHEDULE_INTERRUPT_S: Long = 60 * 10
    }
}
