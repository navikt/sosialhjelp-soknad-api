package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.SOKNAD_SLETTET;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SlettSoknadScheduler {

    private static final Logger logger = getLogger(SlettSoknadScheduler.class);
    private static final String KLOKKEN_FIRE_OM_NATTEN = "0 0 4 * * *";


    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private HenvendelseService henvendelseService;


    @Scheduled(cron = KLOKKEN_FIRE_OM_NATTEN)
    public void opprettHendelserForSoknaderSlettetIHenvendelse(){
        logger.info("Starter sjekk om urørte søknader er slettet i henvendelse");
        soknadRepository.hentIkkeAvsluttedeEtter8Uker().stream().forEach(
                bid -> {
                    try {
                        List<WSBehandlingskjedeElement> result = henvendelseService.hentBehandlingskjede(bid);
                    }
                    catch (ApplicationException a){
                        logger.info("Setter søknad med behandlingsid: " + bid + " til slettet i Henvendelse.");
                        settSoknadAvsluttet(bid);
                    }
                }
        );
    }




    public void settSoknadAvsluttet(String behandlingsId) {
        soknadRepository.insertHendelse(behandlingsId,SOKNAD_SLETTET.name());
    }
}
