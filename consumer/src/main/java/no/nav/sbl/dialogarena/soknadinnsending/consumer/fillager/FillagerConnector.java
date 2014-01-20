package no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class FillagerConnector {

   @Inject
   @Named("fillagerService")
   private FilLagerPortType portType;

    public void lagreFil(String behandlingsId, String uid, InputStream fil) {
        try {
            portType.lagre(behandlingsId, uid, new DataHandler(new ByteArrayDataSource(fil, "application/octet-stream")));
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lagre fil: " + e, e);
        } catch (SOAPFaultException ws) {
            throw new ApplicationException("Feil i kommunikasjon med fillager: " + ws, ws);
        }
    }
}
