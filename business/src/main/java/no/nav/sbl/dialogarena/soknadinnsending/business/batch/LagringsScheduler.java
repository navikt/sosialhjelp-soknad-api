package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

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
    private FillagerConnector fillagerConnector;

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    public void mellomlagreSoknaderOgNullstillLokalDb() throws InterruptedException {
        batchStartTime = DateTime.now();
        vellykket = 0;
        feilet = 0;
        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "true"))) { // TODO: Burde fjernes når applikasjonen skal ut i prod
            logger.info("---- Starter flytting av søknader til henvendelse-jobb ----");
            for (Optional<WebSoknad> ws = soknadRepository.plukkSoknadTilMellomlagring(); ws.isSome(); ws = soknadRepository.plukkSoknadTilMellomlagring()) {
                lagreFilTilHenvendelseOgSlettILokalDb(ws);
                // Avslutt prosessen hvis det er gått for lang tid. Tyder på at noe er nede.
                if (harGaattForLangTid()) {
                    logger.warn("---- Jobben har kjørt i mer enn {} ms. Den blir derfor terminert ----", SCHEDULE_INTERRUPT_MS);
                    return;
                }
            }
            logger.info("---- Jobb fullført: {} vellykket, {} feilet ----", vellykket, feilet);
        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
        }
    }

    protected void lagreFilTilHenvendelseOgSlettILokalDb(Optional<WebSoknad> ws) throws InterruptedException {
        WebSoknad soknad = ws.get();
        try {
            if (soknad.getStatus().equals(SoknadInnsendingStatus.UNDER_ARBEID) && !soknad.erEttersending()) {
                StringWriter xml = new StringWriter();
                JAXB.marshal(soknad, xml);
                fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(xml.toString().getBytes()));
            }
            soknadRepository.slettSoknad(soknad.getSoknadId());
            vellykket++;
            logger.info("---- Lagret soknad til henvendelse og slettet lokalt. Soknadsid: " + soknad.getSoknadId() + "----");
        } catch (Exception e) {
            feilet++;
            logger.error("Lagring eller sletting feilet for soknad {}. Setter tilbake til LEDIG", soknad.getSoknadId(), e);
            try {
                soknadRepository.leggTilbake(soknad);
            } catch (Exception e1) {
                logger.error("Klarte ikke å legge tilbake søknad {}", soknad.getSoknadId(), e1);
            }
            Thread.sleep(1000); // Så loggen ikke blir fylt opp
        }

    }

    private boolean harGaattForLangTid() {
        return DateTime.now().isAfter(batchStartTime.plusMillis(SCHEDULE_INTERRUPT_MS));
    }
}

