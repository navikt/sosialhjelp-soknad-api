package no.nav.sosialhjelp.soknad.innsending.svarut

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.FiksResultat
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.navKontorTilMetricNavn
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class FiksHandterer(
    private val fiksSender: FiksSender,
    private val innsendingService: InnsendingService,
    private val prometheusMetricsService: PrometheusMetricsService
) {
    fun eksekver(oppgave: Oppgave) {
        val behandlingsId = oppgave.behandlingsId
        log.info("Kjører fikskjede for behandlingsid $behandlingsId, steg ${oppgave.steg}")

        val resultat = oppgave.oppgaveResultat
            ?: throw IllegalStateException("Søknad med behandlingsId $behandlingsId har oppgaveResultat=null")
        val eier = oppgave.oppgaveData?.avsenderFodselsnummer
            ?: throw IllegalStateException("Søknad med behandlingsid $behandlingsId har eier=null")

        check(!StringUtils.isEmpty(eier)) { "Søknad med behandlingsid $behandlingsId mangler eier" }

        when (oppgave.steg) {
            21 -> {
                sendTilFiks(behandlingsId, resultat, eier)
                oppgave.nesteSteg()
            }
            22 -> {
                slettSoknadOgFiler(behandlingsId, eier)
                oppgave.nesteSteg()
            }
            else -> {
                lagreResultat(behandlingsId, resultat, eier)
                oppgave.ferdigstill()
            }
        }
    }

    private fun sendTilFiks(behandlingsId: String, resultat: FiksResultat, eier: String) {
        val sendtSoknad = innsendingService.hentSendtSoknad(behandlingsId, eier)
        try {
            resultat.fiksForsendelsesId = fiksSender.sendTilFiks(sendtSoknad)
            prometheusMetricsService.reportSendtMedSvarUt(sendtSoknad.erEttersendelse)
            prometheusMetricsService.reportSoknadMottaker(sendtSoknad.erEttersendelse, navKontorTilMetricNavn(sendtSoknad.navEnhetsnavn))
            log.info("Søknad $behandlingsId fikk id ${resultat.fiksForsendelsesId} i Fiks")
        } catch (e: Exception) {
            resultat.feilmelding = e.message
            prometheusMetricsService.reportFeiletMedSvarUt(sendtSoknad.erEttersendelse)
            throw e
        }
    }

    private fun slettSoknadOgFiler(behandlingsId: String, eier: String) {
        innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(behandlingsId, eier)
    }

    private fun lagreResultat(behandlingsId: String, resultat: FiksResultat, eier: String) {
        innsendingService.oppdaterTabellerVedSendingTilFiks(resultat.fiksForsendelsesId, behandlingsId, eier)
    }

    companion object {
        const val FIKS_OPPGAVE = "FiksOppgave"
        private val log by logger()
    }
}
