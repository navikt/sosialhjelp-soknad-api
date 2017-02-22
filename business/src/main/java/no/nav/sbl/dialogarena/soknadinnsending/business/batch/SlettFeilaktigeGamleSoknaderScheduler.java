package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SlettFeilaktigeGamleSoknaderScheduler {

    private static final Logger logger = getLogger(SlettFeilaktigeGamleSoknaderScheduler.class);
    private static final int SCHEDULE_RATE_MS = 1000 * 60 * 10; // 10 min
    private static final int SCHEDULE_INTERRUPT_MS = 1000 * 30; // 30 sek
    private DateTime batchStartTime;
    private int mellomlagretIHenvendelse;
    private int slettetFraSendsoknad;



    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private FillagerService fillagerService;
    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    @Named("sendSoknadEndpoint")
    private SendSoknadPortType sendSoknadEndpoint;

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    public void startBatchJobb() {
        List<WebSoknad> feilListe = new ArrayList<>();

        batchStartTime = DateTime.now();
        mellomlagretIHenvendelse = 0;
        slettetFraSendsoknad = 0;

        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            logger.info("Starter jobb for å mellomlagre eller slette søknader som har blitt stuck i databasen");

            mellomlagreEllerSlettGamleSoknader(feilListe);
            leggTilbake(feilListe);

            logger.info("Jobb fullført: {} søknader ble slettet fra sendsøknad, av dem ble {} mellomlagret i henvendelse, {} feilet og ble lagt tilbake",
                    slettetFraSendsoknad, mellomlagretIHenvendelse, feilListe.size());
        }

    }

    private void mellomlagreEllerSlettGamleSoknader(List<WebSoknad> feilListe) {
        for (Optional<WebSoknad> ws = soknadRepository.plukkFeillagretSoknadTilSletting(); ws.isSome(); ws = soknadRepository.plukkFeillagretSoknadTilSletting()) {

            WebSoknad soknad = ws.get();

            // Klarer ikke å skille på om Henvendelse er nede eller om søknaden bare ikke finnes når vi etterspør en søknad,
            // da begge deler gir en SOAPException, så pinger Henvendelse først for å kunne vite hva som er tilfelle
            if (!henvendelseErOppe()) {
                feilListe.add(soknad);
                return;
            }

            if (soknadEksistererIHenvendelse(soknad)) {
                if (lagreTilHenvendelse(soknad)) {
                    slettFraLokalDb(soknad);
                } else {
                    feilListe.add(soknad);
                }
            } else {
                slettFraLokalDb(soknad);
            }


            // Avslutt prosessen hvis det er gått for lang tid
            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} ms. Den blir derfor terminert", SCHEDULE_INTERRUPT_MS);
                return;
            }
        }
    }

    private boolean henvendelseErOppe() {
        try {
            sendSoknadEndpoint.ping();
            return true;
        } catch (Exception e) {
            logger.warn("Feilet å pinge henvendelse", e);
            return false;
        }
    }

    private void leggTilbake(List<WebSoknad> feilliste) {
        for (WebSoknad soknad : feilliste) {
            soknadRepository.leggTilbake(soknad);
        }
    }

    private boolean soknadEksistererIHenvendelse(WebSoknad soknad) {
        try {
            String behandlingsId = soknad.getBrukerBehandlingId();
            WSHentSoknadResponse response = henvendelseService.hentSoknad(behandlingsId);
            return behandlingsId.equals(response.getBehandlingsId());
        } catch (Exception e) {
            // Får en generisk exception om Henvendelse ikke finner søknaden
            return false;
        }
    }

    private boolean lagreTilHenvendelse(WebSoknad soknad) {
        try {
            StringWriter xml = new StringWriter();
            JAXB.marshal(soknad, xml);
            fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(xml.toString().getBytes()));
            mellomlagretIHenvendelse++;
            logger.info("Lagret søknad {} i henvendelse", soknad.getBrukerBehandlingId());
            return true;
        } catch (Exception e) {
            logger.error("Lagring i henvendelse feilet for søknad {}", soknad.getBrukerBehandlingId(), e);
            return false;
        }
    }

    private void slettFraLokalDb(WebSoknad soknad) {
        soknadRepository.slettSoknad(soknad.getSoknadId());
        logger.info("Slettet soknad fra lokal db, soknadid: {}, behandlingsid: {}", soknad.getSoknadId(), soknad.getBrukerBehandlingId());
        slettetFraSendsoknad++;
    }

    private boolean harGaattForLangTid() {
        return DateTime.now().isAfter(batchStartTime.plusMillis(SCHEDULE_INTERRUPT_MS));
    }
}

