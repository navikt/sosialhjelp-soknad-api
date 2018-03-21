package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class FiksHandterer {

    public static final String FIKS_OPPGAVE = "FiksOppgave";
    private static final Logger logger = LoggerFactory.getLogger(FiksHandterer.class);

    @Inject
    private MetadataInnfyller metadataInnfyller;


    public void eksekver(Oppgave oppgaveKjede) {
        logger.info("Kjører fikskjede for behandlingsid {}, steg {}", oppgaveKjede.behandlingsId, oppgaveKjede.steg);

        FiksData data = oppgaveKjede.oppgaveData;
        FiksResultat resultat = oppgaveKjede.oppgaveResultat;

        if (oppgaveKjede.steg == 0) {
            data.behandlingsId = oppgaveKjede.behandlingsId;
            metadataInnfyller.byggOppFiksData(data);
            oppgaveKjede.nesteSteg();
        } else if (oppgaveKjede.steg == 1) {
            Event event = MetricsFactory.createEvent("digisos.fiks.sendt");
            event.addTagToReport("mottaker", data.mottakerNavn);
            try {
//                resultat.fiksForsendelsesId  = fiksSender.sendTilFiks(data);
                logger.info("Søknad {} fikk id {} i Fiks", data.behandlingsId, resultat.fiksForsendelsesId );
            } catch (Exception e) {
                resultat.feilmelding = e.getMessage();
                event.setFailed();
                throw e;
            } finally {
                event.report();
            }
            oppgaveKjede.nesteSteg();
        } else if (oppgaveKjede.steg == 2) {
//            henvendelseFiksOperasjoner.slettVedleggFraHenvendelse(data);
            oppgaveKjede.nesteSteg();
        } else {
            // + evt. sende id-en til et annet system?
            oppgaveKjede.ferdigstill();
        }
    }
}
