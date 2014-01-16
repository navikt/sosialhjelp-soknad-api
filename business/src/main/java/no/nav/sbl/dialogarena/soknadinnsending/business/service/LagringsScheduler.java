package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
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
    private static final int SCHEDULE_RATE_MS = 1000*60*60; //1 time

    @Inject
    SoknadRepository soknadRepository;
    @Inject
    FillagerConnector fillagerConnector;

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    private void mellomlagreSoknaderOgNullstillLokalDb() {
        LOG.info("entered mellomlagreSoknaderOgNullstillLokalDb");
        for (Long id : soknadRepository.hentAlleSoknaderSistLagretOverEnTimeSiden()) {
            StringWriter xml = new StringWriter();
            WebSoknad soknad = soknadRepository.hentSoknadMedData(id);
            JAXB.marshal(soknad, xml);
//            henvendelseConnector.mellomlagreSoknad(soknad.getBrukerBehandlingId(), xml.toString());
            fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(), "", new ByteArrayInputStream(xml.toString().getBytes()));
            slettSoknadFraDb(soknad.getSoknadId());
        }
    }

    private void slettSoknadFraDb(long soknadsId) {
        soknadRepository.slettSoknad(soknadsId);
    }

}

