package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.util.List;

@Service
public class LagringsScheduler {

    @Inject
    SoknadRepository soknadRepository;
    @Inject
    HenvendelseConnector henvendelseConnector;

    //TODO: Øke til et større intervall enn 5 sek
    @Scheduled(fixedRate = 5000)
    private void mellomlagreSoknad() {
        List<WebSoknad> soknader = soknadRepository.hentAlleSoknaderSistLagretUnderEnTimeSiden();

        for (WebSoknad soknad : soknader) {
            StringWriter xml = new StringWriter();
            JAXB.marshal(soknad, xml);
            henvendelseConnector.mellomlagreSoknad(soknad.getBrukerBehandlingId(), xml.toString());
        }
    }
}
