package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.Collection;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.SOKNAD_SLETTET;

import static org.slf4j.LoggerFactory.getLogger;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.*;

@Service
public class SlettSoknadScheduler {

    private static final Logger logger = getLogger(SlettSoknadScheduler.class);
    private static final String KLOKKEN_FEM_OM_NATTEN = "0 0 5 * * *";
    private static final long MAKS_KJORETID_MS = 1000 * 60 * 10; // Ti minutter


    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private HenvendelseService henvendelseService;


    @Scheduled(cron = KLOKKEN_FEM_OM_NATTEN)
    public void opprettHendelserForSoknaderSlettetIHenvendelse(){
        if (Boolean.valueOf(System.getProperty("cluster.ismasternode", "false"))) {
            Event event = MetricsFactory.createEvent("soknad.hendelse.avsluttAutomatisk");
            logger.info("Kron: " + KLOKKEN_FEM_OM_NATTEN);
            logger.info("Starter sjekk om urørte søknader er slettet i henvendelse");

            long start = System.currentTimeMillis();

            Collection<String> ikkeAvsluttede = soknadRepository.hentIkkeAvsluttedeEtter8Uker();
            int counter = 0;
            int maksAntall = soknadRepository.hentIkkeAvsluttedeEtter8Uker().size();
            event.addTagToReport("antallIkkeAvsluttede","" + maksAntall);

            for(String behandlingsid : ikkeAvsluttede){
                WSHentSoknadResponse wsHentSoknadResponse = henvendelseService.hentSoknad(behandlingsid);
                if(AVBRUTT_AUTOMATISK.equals(SoknadInnsendingStatus.valueOf(wsHentSoknadResponse.getStatus()))){
                    settSoknadAvsluttet(behandlingsid);
                    event.addTagToReport("avslutterSoknad",behandlingsid);
                    counter++;
                }
                if ((System.currentTimeMillis() - start > MAKS_KJORETID_MS) || counter > maksAntall) {
                    logger.info("Har kjørt for lenge eller maks antall slettet er oppnådd. Avbryter for å unngå å ta for mye ressurser");
                    break;
                }
            }

            event.addTagToReport("antallAvsluttet","" + counter);
            event.report();
        }
    }

    private void settSoknadAvsluttet(String behandlingsId) {
        soknadRepository.insertHendelse(behandlingsId,SOKNAD_SLETTET.name());
    }
}
