package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class LagringsScheduler {

    private static final Logger logger = getLogger(LagringsScheduler.class);
    private static final int SCHEDULE_RATE_MS = 1000 * 60 * 60; // 1 time
    private static final int SCHEDULE_INTERRUPT_MS = 1000 * 60 * 10; // 10 min
    private DateTime batchStartTime;
    private int vellykket;
    private int feilet;

    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private FillagerService fillagerService;
    @Inject
    private HenvendelseService henvendelseService;

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    public void mellomlagreSoknaderOgNullstillLokalDb() throws InterruptedException {
        List<Optional<WebSoknad>> feilListe = new ArrayList<>();
        batchStartTime = DateTime.now();
        vellykket = 0;
        feilet = 0;
        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            logger.info("Starter flytting av søknader til henvendelse-jobb");

            mellomlagre(feilListe);
            leggTilbakeFeilende(feilListe);

            logger.info("Jobb fullført: {} vellykket, {} feilet", vellykket, feilet);
        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
        }
    }

    private void leggTilbakeFeilende(List<Optional<WebSoknad>> feilListe) {
        for (Optional<WebSoknad> ws : feilListe) {
            WebSoknad soknad = ws.get();
            try {
                soknadRepository.leggTilbake(soknad);
            } catch (Exception e1) {
                logger.error("Klarte ikke å legge tilbake søknad {}", soknad.getSoknadId(), e1);
            }
        }
    }

    private void mellomlagre(List<Optional<WebSoknad>> feilListe) throws InterruptedException {
        for (Optional<WebSoknad> ws = soknadRepository.plukkSoknadTilMellomlagring(); ws.isPresent(); ws = soknadRepository.plukkSoknadTilMellomlagring()) {
            if (isPaabegyntEttersendelse(ws)) {
                if (!avbrytOgSlettEttersendelse(ws)) {
                    feilListe.add(ws);
                }
            } else {
                if (!lagreFilTilHenvendelseOgSlettILokalDb(ws)) {
                    feilListe.add(ws);
                }
            }
            // Avslutt prosessen hvis det er gått for lang tid. Tyder på at noe er nede.
            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} ms. Den blir derfor terminert", SCHEDULE_INTERRUPT_MS);
                return;
            }
        }
    }

    private boolean avbrytOgSlettEttersendelse(Optional<WebSoknad> ws) throws InterruptedException {
        WebSoknad soknad = ws.get();
        try {
            henvendelseService.avbrytSoknad(soknad.getBrukerBehandlingId());
            slettFiler(soknad);
            soknadRepository.slettSoknad(soknad.getSoknadId());

            vellykket++;
            return true;
        } catch (Exception e) {
            feilet++;
            logger.error("Avbryt feilet for ettersending {}.", soknad.getSoknadId(), e);
            Thread.sleep(1000); // Så loggen ikke blir fylt opp

            return false;
        }
    }

    private void slettFiler(WebSoknad soknad) {
        try {
            fillagerService.slettAlle(soknad.getBrukerBehandlingId());
        } catch (Exception e) {
            logger.error("Sletting av filer feilet for ettersending {}. Henvendelsen de hører til er satt til avbrutt, og ettersendingen slettes i sendsøknad.", soknad.getSoknadId(), e);
        }
    }

    private boolean isPaabegyntEttersendelse(Optional<WebSoknad> ws) {
        WebSoknad soknad = ws.get();
        return soknad.erEttersending();
    }

    protected boolean lagreFilTilHenvendelseOgSlettILokalDb(Optional<WebSoknad> ws) throws InterruptedException {
        WebSoknad soknad = ws.get();
        try {
            if (soknad.getStatus().equals(SoknadInnsendingStatus.UNDER_ARBEID) && !soknad.erEttersending()) {
                StringWriter xml = new StringWriter();
                JAXB.marshal(soknad, xml);
                fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getFodselsnummer(), new ByteArrayInputStream(xml.toString().getBytes()));
            }
            soknadRepository.slettSoknad(soknad.getSoknadId());

            logger.info("Lagret soknad til henvendelse og slettet lokalt. Soknadsid: {}", soknad.getSoknadId());
            vellykket++;
            return true;
        } catch (Exception e) {
            feilet++;
            logger.error("Lagring eller sletting feilet for soknad {}", soknad.getSoknadId(), e);

            Thread.sleep(1000); // Så loggen ikke blir fylt opp
            return false;
        }
    }

    private boolean harGaattForLangTid() {
        return DateTime.now().isAfter(batchStartTime.plusMillis(SCHEDULE_INTERRUPT_MS));
    }
}

