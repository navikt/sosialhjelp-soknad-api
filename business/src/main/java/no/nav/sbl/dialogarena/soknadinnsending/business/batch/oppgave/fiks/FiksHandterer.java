package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.sosialhjelp.InnsendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class FiksHandterer {

    public static final String FIKS_OPPGAVE = "FiksOppgave";
    private static final Logger logger = LoggerFactory.getLogger(FiksHandterer.class);

    @Inject
    private MetadataInnfyller metadataInnfyller;

    @Inject
    private FiksSender fiksSender;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private InnsendingService innsendingService;


    public void eksekver(Oppgave oppgaveKjede) {
        logger.info("Kjører fikskjede for behandlingsid {}, steg {}", oppgaveKjede.behandlingsId, oppgaveKjede.steg);

        FiksData data = oppgaveKjede.oppgaveData;
        FiksResultat resultat = oppgaveKjede.oppgaveResultat;

        if (oppgaveKjede.steg == 0) {
            data.behandlingsId = oppgaveKjede.behandlingsId;
            metadataInnfyller.byggOppFiksData(data);
            oppgaveKjede.nesteSteg();
        } else if (oppgaveKjede.steg == 1) {
            Event event = MetricsFactory.createEvent("digisos.fikshandterer.sendt");
            event.addTagToReport("ettersendelse", isEmpty(data.ettersendelsePa) ? "false" : "true");
            event.addTagToReport("mottaker", tilInfluxNavn(data.mottakerNavn));
            try {
                resultat.fiksForsendelsesId = fiksSender.sendTilFiks(data);
                logger.info("Søknad {} fikk id {} i Fiks", data.behandlingsId, resultat.fiksForsendelsesId);
            } catch (Exception e) {
                resultat.feilmelding = e.getMessage();
                event.setFailed();
                throw e;
            } finally {
                event.report();
            }
            oppgaveKjede.nesteSteg();
        } else if (oppgaveKjede.steg == 2) {
            innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(oppgaveKjede.behandlingsId, oppgaveKjede.oppgaveData.avsenderFodselsnummer);
            fillagerService.slettAlle(data.behandlingsId);
            oppgaveKjede.nesteSteg();
        } else {
            metadataInnfyller.lagreFiksId(data, resultat);
            innsendingService.oppdaterSendtSoknadVedSendingTilFiks(resultat.fiksForsendelsesId, data.behandlingsId, data.avsenderFodselsnummer);
            oppgaveKjede.ferdigstill();
        }
    }

    private String tilInfluxNavn(String mottaker) {
        if (mottaker == null) {
            return "";
        }
        return mottaker
                .replace("NAV", "")
                .replace(",", "");
    }
}
