package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.StringWriter;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class LagringsScheduler {

    private static final Logger LOG = getLogger(LagringsScheduler.class);

    @Inject
    SoknadRepository soknadRepository;
    @Inject
    HenvendelseConnector henvendelseConnector;

    //TODO: Øke til et større intervall enn 5 sek
    @Scheduled(fixedRate = 5000)
    private void mellomlagreSoknaderOgNullstillLokalDb() {
        LOG.info("entered mellomlagreSoknaderOgNullstillLokalDb");
        for (WebSoknad soknad : soknadRepository.hentAlleSoknaderSistLagretOverEnTimeSiden()) {
            StringWriter xml = new StringWriter();
            JAXB.marshal(soknad, xml);
            henvendelseConnector.mellomlagreSoknad(soknad.getBrukerBehandlingId(), xml.toString());
            slettSoknadFraDb(soknad.getSoknadId());
        }
    }

    private void slettSoknadFraDb(long soknadsId) {
        soknadRepository.slettSoknad(soknadsId);
    }

}
