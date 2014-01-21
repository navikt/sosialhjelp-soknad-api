package no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FillagerConnector {

    private static final Logger LOG = getLogger(FillagerConnector.class);

    @Inject
    @Named("fillagerService")
    private FilLagerPortType portType;

    public void lagreFil(String behandlingsId, String uid, String fnr, InputStream fil) {
        LOG.info("Skal lagre soknad til henvendelse. UUID: " + uid + ". Behandlingsid: " + behandlingsId);
        try {
            portType.lagre(behandlingsId, uid, fnr, new DataHandler(new ByteArrayDataSource(fil, "application/octet-stream")));
            LOG.info("Søknad lagret til henvendelse");
        } catch (IOException e) {
            LOG.error("Fikk ikke lagret søknad til henvendelse");
            throw new ApplicationException("Kunne ikke lagre fil: " + e + ". BehandlingsID: " + behandlingsId, e);
        } catch (SOAPFaultException ws) {
            LOG.error("Fikk ikke lagret søknad til henvendelse");
            throw new SystemException("Feil i kommunikasjon med fillager: " + ws + ". BehandlingsID: " + behandlingsId, ws);
        }
    }
}
