package no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class FillagerConnector {

   // @Inject
   // @Named("fillagerService")
   // private FilLagerPortType portType;

    private static final Logger logger = LoggerFactory.getLogger(FillagerConnector.class);

    public void lagreFil(String behandlingsId, String uid, InputStream fil) {
//        try {  /
//            portType.lagre(behandlingsId, uid, new DataHandler(new ByteArrayDataSource(fil, "application/octet-stream")));
//        } catch (IOException e) {
//            throw new ApplicationException("Kunne ikke lagre fil: " + e, e);
//        } catch (SOAPFaultException ws) {
//            throw new ApplicationException("Feil i kommunikasjon med fillager: " + ws, ws);
//        }
            logger.info("Later som jeg lagrer fil {} for behandlingsid {} og uuio {}", fil, behandlingsId, uid);
    }
}
