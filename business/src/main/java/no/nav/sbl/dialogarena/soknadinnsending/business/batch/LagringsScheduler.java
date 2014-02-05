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

    private static final Logger LOG = getLogger(LagringsScheduler.class);
    private static final int SCHEDULE_RATE_MS = 1000 * 60 * 60; // 1 time
    private static final int SCHEDULE_INTERRUPT_MS = 1000 * 60; // 1 min
    private DateTime batchStartTime;

    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private FillagerConnector fillagerConnector;

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    public void mellomlagreSoknaderOgNullstillLokalDb() throws InterruptedException {
        batchStartTime = DateTime.now();
        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "false"))) { // TODO: Burde fjernes når applikasjonen skal ut i prod
            LOG.info("entered mellomlagreSoknaderOgNullstillLokalDb");
            for (Optional<WebSoknad> ws = soknadRepository.plukkSoknadTilMellomlagring(); ws.isSome(); ws = soknadRepository.plukkSoknadTilMellomlagring()) {
                lagreFilTilHenvendelseOgSlettILokalDb(ws);
                // Avslutt prosessen hvis det er gått for lang tid. Tyder på at noe er nede.
                if (harGaattForLangTid()) {
                    return;
                }
            }
        } else {
            LOG.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
        }
    }

    protected void lagreFilTilHenvendelseOgSlettILokalDb(Optional<WebSoknad> ws) throws InterruptedException {
        WebSoknad soknad = ws.get();
        try {
            StringWriter xml = new StringWriter();
            JAXB.marshal(soknad, xml);
            fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(xml.toString().getBytes()));
            soknadRepository.slettSoknad(soknad.getSoknadId());
            LOG.info("---- Lagret soknad til henvendelse og slettet lokalt. Soknadsid: " + soknad.getUuid() + "----");
        } catch (Exception e) {
            LOG.error("Feil", e);
            soknadRepository.leggTilbake(soknad);
            Thread.sleep(1000); // Så loggen ikke blir fylt opp
        }

    }

    private boolean harGaattForLangTid() {
        return DateTime.now().isAfter(batchStartTime.plusMillis(SCHEDULE_INTERRUPT_MS));
    }
}

