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
            log.warn("Dkif.api - response inneholder feil - {}", digitalKontaktinfoBolk.getFeil().get(ident).getMelding());
            return null;
        }
        if (digitalKontaktinfoBolk.getKontaktinfo() == null || digitalKontaktinfoBolk.getKontaktinfo().isEmpty() || !digitalKontaktinfoBolk.getKontaktinfo().containsKey(ident) || digitalKontaktinfoBolk.getKontaktinfo().get(ident).getMobiltelefonnummer() == null) {
            log.warn("Dkif.api - kontaktinfo er null, eller mobiltelefonnummer er null");
            return null;
        }
        return digitalKontaktinfoBolk.getKontaktinfo().get(ident).getMobiltelefonnummer();
    }

    private String hentMobiltelefonnummerWS(String ident) {
        DigitalKontaktinfo digitalKontaktinfo = epostService.hentInfoFraDKIF(ident);
        return digitalKontaktinfo.getMobilnummer();
    }
}
