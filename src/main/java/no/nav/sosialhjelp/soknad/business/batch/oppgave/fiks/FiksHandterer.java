package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import no.nav.sosialhjelp.metrics.Event;
import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.innsending.InnsendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static no.nav.sosialhjelp.soknad.business.util.MetricsUtils.navKontorTilInfluxNavn;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class FiksHandterer {

    public static final String FIKS_OPPGAVE = "FiksOppgave";
    private static final Logger logger = LoggerFactory.getLogger(FiksHandterer.class);

    private final FiksSender fiksSender;
    private final InnsendingService innsendingService;

    public FiksHandterer(FiksSender fiksSender, InnsendingService innsendingService) {
        this.fiksSender = fiksSender;
        this.innsendingService = innsendingService;
    }

    public void eksekver(Oppgave oppgaveKjede) {
        final String behandlingsId = oppgaveKjede.behandlingsId;
        logger.info("Kjører fikskjede for behandlingsid {}, steg {}", behandlingsId, oppgaveKjede.steg);

        FiksResultat resultat = oppgaveKjede.oppgaveResultat;
        final String eier = oppgaveKjede.oppgaveData.avsenderFodselsnummer;
        if (isEmpty(eier)) {
            throw new IllegalStateException("Søknad med behandlingsid " + behandlingsId + " mangler eier");
        }
        if (oppgaveKjede.steg == 21) {
            sendTilFiks(behandlingsId, resultat, eier);
            oppgaveKjede.nesteSteg();
        } else if (oppgaveKjede.steg == 22) {
            slettSoknadOgFiler(behandlingsId, eier);
            oppgaveKjede.nesteSteg();
        } else {
            lagreResultat(behandlingsId, resultat, eier);
            oppgaveKjede.ferdigstill();
        }
    }

    private void sendTilFiks(String behandlingsId, FiksResultat resultat, String eier) {
        final SendtSoknad sendtSoknad = innsendingService.hentSendtSoknad(behandlingsId, eier);
        Event event = lagForsoktSendtTilFiksEvent(sendtSoknad);
        try {
            resultat.fiksForsendelsesId = fiksSender.sendTilFiks(sendtSoknad);
            logger.info("Søknad {} fikk id {} i Fiks", behandlingsId, resultat.fiksForsendelsesId);
        } catch (Exception e) {
            resultat.feilmelding = e.getMessage();
            event.setFailed();
            throw e;
        } finally {
            event.report();
        }
    }

    private void slettSoknadOgFiler(String behandlingsId, String eier) {
        innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(behandlingsId, eier);
    }

    private void lagreResultat(String behandlingsId, FiksResultat resultat, String eier) {
        innsendingService.oppdaterSendtSoknadVedSendingTilFiks(resultat.fiksForsendelsesId, behandlingsId, eier);
    }

    private Event lagForsoktSendtTilFiksEvent(SendtSoknad sendtSoknad) {
        Event event = MetricsFactory.createEvent("digisos.fikshandterer.sendt");
        event.addTagToReport("ettersendelse", sendtSoknad.erEttersendelse() ? "true" : "false");
        event.addTagToReport("mottaker", navKontorTilInfluxNavn(sendtSoknad.getNavEnhetsnavn()));
        return event;
    }
}
