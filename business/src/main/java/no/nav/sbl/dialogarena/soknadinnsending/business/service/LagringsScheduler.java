package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.exception.SystemException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class LagringsScheduler {

    private static final Logger LOG = getLogger(LagringsScheduler.class);
   // private static final int SCHEDULE_RATE_MS = 1000*60*60; //1 time

    @Inject
    SoknadRepository soknadRepository;
    @Inject
    FillagerConnector fillagerConnector;

   // @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    private void mellomlagreSoknaderOgNullstillLokalDb() {
        LOG.info("entered mellomlagreSoknaderOgNullstillLokalDb");
        for (Long id : soknadRepository.hentAlleSoknaderSistLagretOverEnTimeSiden()) {
            StringWriter xml = new StringWriter();
            WebSoknad soknad = soknadRepository.hentSoknadMedData(id);
            JAXB.marshal(soknad, xml);
            lagreFilTilHenvendelseOgSlettILokalDb(xml, soknad);
        }
    }

    private void lagreFilTilHenvendelseOgSlettILokalDb(StringWriter xml, WebSoknad soknad) {
        try {
            fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(xml.toString().getBytes()));
            soknadRepository.slettSoknad(soknad.getSoknadId());
            LOG.info("---- Lagret soknad til henvendelse og slettet lokalt. Soknadsid: " + soknad.getUuid() + "----");
        } catch (SOAPFaultException | SystemException e) {
            LOG.error("Klarte ikke lagre søknad til henvendelse. Avbrøt sletting lokalt. Søknad med uuid: " + soknad.getUuid() + ". Feilmelding: " + e.getMessage());
        }
    }
}

