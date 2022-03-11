package no.nav.sosialhjelp.soknad.innsending.svarut

import no.nav.sosialhjelp.metrics.Event
import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.domain.FiksResultat
import no.nav.sosialhjelp.soknad.domain.Oppgave
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.navKontorTilInfluxNavn
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class FiksHandterer(
    private val fiksSender: FiksSender,
    private val innsendingService: InnsendingService
) {
    fun eksekver(oppgaveKjede: Oppgave) {
        val behandlingsId = oppgaveKjede.behandlingsId
        logger.info("Kjører fikskjede for behandlingsid $behandlingsId, steg ${oppgaveKjede.steg}")

        val resultat = oppgaveKjede.oppgaveResultat
        val eier = oppgaveKjede.oppgaveData.avsenderFodselsnummer
        check(!StringUtils.isEmpty(eier)) { "Søknad med behandlingsid $behandlingsId mangler eier" }

        when (oppgaveKjede.steg) {
            21 -> {
                sendTilFiks(behandlingsId, resultat, eier)
                oppgaveKjede.nesteSteg()
            }
            22 -> {
                slettSoknadOgFiler(behandlingsId, eier)
                oppgaveKjede.nesteSteg()
            }
            else -> {
                lagreResultat(behandlingsId, resultat, eier)
                oppgaveKjede.ferdigstill()
            }
        }
    }

    private fun sendTilFiks(behandlingsId: String, resultat: FiksResultat, eier: String) {
        val sendtSoknad = innsendingService.hentSendtSoknad(behandlingsId, eier)
        val event = lagForsoktSendtTilFiksEvent(sendtSoknad)
        try {
            resultat.fiksForsendelsesId = fiksSender.sendTilFiks(sendtSoknad)
            logger.info("Søknad $behandlingsId fikk id ${resultat.fiksForsendelsesId} i Fiks")
        } catch (e: Exception) {
            resultat.feilmelding = e.message
            event.setFailed()
            throw e
        } finally {
            event.report()
        }
    }

    private fun slettSoknadOgFiler(behandlingsId: String, eier: String) {
        innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(behandlingsId, eier)
    }

    private fun lagreResultat(behandlingsId: String, resultat: FiksResultat, eier: String) {
        innsendingService.oppdaterSendtSoknadVedSendingTilFiks(resultat.fiksForsendelsesId, behandlingsId, eier)
    }

    private fun lagForsoktSendtTilFiksEvent(sendtSoknad: SendtSoknad): Event {
        val event = MetricsFactory.createEvent("digisos.fikshandterer.sendt")
        event.addTagToReport("ettersendelse", if (sendtSoknad.erEttersendelse) "true" else "false")
        event.addTagToReport("mottaker", navKontorTilInfluxNavn(sendtSoknad.navEnhetsnavn))
        return event
    }

    companion object {
        const val FIKS_OPPGAVE = "FiksOppgave"
        private val logger = LoggerFactory.getLogger(FiksHandterer::class.java)
    }
}
