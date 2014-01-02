package no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.io.InputStream;

public class FillagerConnector {
    private FilLagerPortType portType;

    public void lagreFil(String uid, InputStream fil) {
        try {
            portType.lagre(uid, new DataHandler(new ByteArrayDataSource(fil, "application/octet-stream")));
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lagre fil: " + e, e);
        } catch (SOAPFaultException ws) {
            throw new ApplicationException("Feil i kommunikasjon med fillager: " + ws, ws);
        }
    }
}
