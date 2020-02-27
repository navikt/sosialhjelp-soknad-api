package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif;

import no.nav.sbl.dialogarena.sendsoknad.domain.DigitalKontaktinfo;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto.DigitalKontaktinfoBolk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.lang.System.getProperty;

@Component
public class DkifService {

    private static final Logger log = LoggerFactory.getLogger(DkifService.class);
    private static final String DKIF_API_ENABLED = "dkif_api_enabled";

    private DkifConsumer consumer;

    private EpostService epostService;

    public DkifService(DkifConsumer consumer, EpostService epostService) {
        this.consumer = consumer;
        this.epostService = epostService;
    }

    public boolean brukDkifApi() {
        return Boolean.parseBoolean(getProperty(DKIF_API_ENABLED, "false"));
    }

    public String hentMobiltelefonnummer(String ident) {
        return brukDkifApi() ? hentMobiltelefonnummerRest(ident) : hentMobiltelefonnummerWS(ident);
    }

    private String hentMobiltelefonnummerRest(String ident) {
        DigitalKontaktinfoBolk digitalKontaktinfoBolk = consumer.hentDigitalKontaktinfo(ident);
        if (digitalKontaktinfoBolk == null) {
            log.warn("Dkif.api - response er null");
            return null;
        }
        if (digitalKontaktinfoBolk.getFeil() != null) {
            log.warn("Dkif.api - response inneholder feil - {}", digitalKontaktinfoBolk.getFeil().get(0).getMelding());
            return null;
        }
        if (digitalKontaktinfoBolk.getKontaktinfo() == null || digitalKontaktinfoBolk.getKontaktinfo().get(0).getMobiltelefonnummer() == null) {
            log.warn("Dkif.api - kontaktinfo er null, eller inneholder ikke mobiltelefonnummer");
            return null;
        }
        log.info("Hentet mobiltelefonnummer: {}", digitalKontaktinfoBolk.getKontaktinfo().get(0).getMobiltelefonnummer());
        return digitalKontaktinfoBolk.getKontaktinfo().get(0).getMobiltelefonnummer();
    }

    private String hentMobiltelefonnummerWS(String ident) {
        DigitalKontaktinfo digitalKontaktinfo = epostService.hentInfoFraDKIF(ident);
        return digitalKontaktinfo.getMobilnummer();
    }
}
