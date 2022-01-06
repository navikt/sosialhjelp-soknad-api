package no.nav.sosialhjelp.soknad.innsending.svarut

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave.Status
import no.nav.sosialhjelp.soknad.business.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.common.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isScheduledTasksDisabled
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import kotlin.math.pow

interface OppgaveHandterer {
    fun leggTilOppgave(behandlingsId: String, eier: String)
}

class OppgaveHandtererImpl(
    private val fiksHandterer: FiksHandterer,
    private val oppgaveRepository: OppgaveRepository
) : OppgaveHandterer {

    @Scheduled(fixedDelay = PROSESS_RATE)
    fun prosesserOppgaver() {
        if (isScheduledTasksDisabled()) {
            logger.info("Scheduler is disabled")
            return
        }
        while (true) {
            val oppgaveOptional = oppgaveRepository.hentNeste()
            if (oppgaveOptional.isEmpty) {
                return
            }
            val oppgave = oppgaveOptional.get()
            val event = MetricsFactory.createEvent("digisos.oppgaver")
            event.addTagToReport("oppgavetype", oppgave.type)
            event.addTagToReport("steg", oppgave.steg.toString() + "")
            event.addFieldToReport("behandlingsid", oppgave.behandlingsId)

            MDCOperations.putToMDC(MDCOperations.MDC_BEHANDLINGS_ID, oppgave.behandlingsId)

            try {
                fiksHandterer.eksekver(oppgave)
            } catch (e: Exception) {
                logger.error("Oppgave feilet, id: ${oppgave.id}, beh: ${oppgave.behandlingsId}", e)
                oppgaveFeilet(oppgave)
                event.setFailed()
            }
            event.report()

            if (oppgave.status == Status.UNDER_ARBEID) {
                oppgave.status = Status.KLAR
            }
            oppgaveRepository.oppdater(oppgave)
            MDCOperations.remove(MDCOperations.MDC_BEHANDLINGS_ID)
        }
    }

    @Scheduled(fixedDelay = RETRY_STUCK_RATE)
    fun retryStuckUnderArbeid() {
        if (isScheduledTasksDisabled()) {
            logger.info("Scheduler is disabled")
            return
        }
        try {
            val antall = oppgaveRepository.retryOppgaveStuckUnderArbeid()
            if (antall > 0) {
                logger.info("Har satt $antall oppgaver tilbake til KLAR etter at de lå for lenge som UNDER_ARBEID.")
            }
        } catch (e: Exception) {
            logger.error("Uventet feil ved oppdatering av oppgaver som er stuck i UNDER_ARBEID")
        }
    }

    @Scheduled(fixedRate = RAPPORTER_RATE)
    fun rapporterFeilede() {
        if (isScheduledTasksDisabled()) {
            logger.info("Scheduler is disabled")
            return
        }
        val statuser = oppgaveRepository.hentStatus()
        statuser.forEach { (key, value) ->
            logger.info("Databasestatus for oppgaver: $key er $value")
            val event = MetricsFactory.createEvent("status.oppgave.$key")
            event.addFieldToReport("antall", value)
            event.report()
        }
    }

    private fun oppgaveFeilet(oppgave: Oppgave) {
        oppgave.retries++
        if (oppgave.retries > FEIL_THRESHOLD) {
            oppgave.status = Status.FEILET
        } else {
            oppgave.nesteForsok = nesteForsokEksponensiellBackoff(oppgave.retries)
        }
    }

    private fun nesteForsokEksponensiellBackoff(antallForsok: Int): LocalDateTime {
        val backoff = LocalDateTime.now().plusMinutes(2.0.pow(antallForsok.toDouble()).toInt().toLong())
        val max = LocalDateTime.now().plusHours(1)
        return if (backoff.isBefore(max)) backoff else max
    }

    override fun leggTilOppgave(behandlingsId: String, eier: String) {
        val oppgave = Oppgave()
        oppgave.behandlingsId = behandlingsId
        oppgave.type = FiksHandterer.FIKS_OPPGAVE
        oppgave.status = Status.KLAR
        oppgave.opprettet = LocalDateTime.now()
        oppgave.nesteForsok = LocalDateTime.now()
        oppgave.steg = FORSTE_STEG_NY_INNSENDING
        oppgave.retries = 0
        oppgave.oppgaveData.avsenderFodselsnummer = eier
        oppgaveRepository.opprett(oppgave)
    }

    companion object {
        const val FORSTE_STEG_NY_INNSENDING = 21
        private val logger = LoggerFactory.getLogger(OppgaveHandtererImpl::class.java)
        private const val FEIL_THRESHOLD = 20
        private const val PROSESS_RATE: Long = 10 * 1000 // 10 sek etter forrige
        private const val RAPPORTER_RATE: Long = 15 * 60 * 1000 // hvert kvarter
        private const val RETRY_STUCK_RATE: Long = 15 * 60 * 1000 // hvert kvarter
    }
}
