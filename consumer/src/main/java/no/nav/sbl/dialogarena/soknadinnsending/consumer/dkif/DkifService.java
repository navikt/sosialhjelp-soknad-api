package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto.DigitalKontaktinfoBolk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DkifService {

    private static final Logger log = LoggerFactory.getLogger(DkifService.class);

    private DkifConsumer consumer;

    public DkifService(DkifConsumer consumer) {
        this.consumer = consumer;
    }

    public String hentMobiltelefonnummer(String ident) {
        DigitalKontaktinfoBolk digitalKontaktinfoBolk = consumer.hentDigitalKontaktinfo(ident);
        if (digitalKontaktinfoBolk == null) {
            log.warn("Dkif.api - response er null");
            return null;
        }
        if (digitalKontaktinfoBolk.getFeil() != null) {
            log.warn("Dkif.api - response inneholder feil - {}", digitalKontaktinfoBolk.getFeil().get(ident).getMelding());
            return null;
        }
        if (digitalKontaktinfoBolk.getKontaktinfo() == null || digitalKontaktinfoBolk.getKontaktinfo().isEmpty() || !digitalKontaktinfoBolk.getKontaktinfo().containsKey(ident) || digitalKontaktinfoBolk.getKontaktinfo().get(ident).getMobiltelefonnummer() == null) {
            log.warn("Dkif.api - kontaktinfo er null, eller mobiltelefonnummer er null");
            return null;
        }
        return digitalKontaktinfoBolk.getKontaktinfo().get(ident).getMobiltelefonnummer();
    }
}
