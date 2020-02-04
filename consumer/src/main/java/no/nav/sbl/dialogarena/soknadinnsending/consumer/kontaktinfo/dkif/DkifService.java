package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif;

import no.nav.sbl.dialogarena.sendsoknad.domain.DigitalKontaktinfo;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfoBolk;
import org.springframework.stereotype.Component;

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
        return Boolean.parseBoolean(System.getProperty(DKIF_API_ENABLED, "false"));
    }

    public String hentMobiltelefonnummer(String orgnr) {
        return brukDkifApi() ? hentMobiltelefonnummerRest(orgnr) : hentMobiltelefonnummerWS(orgnr);
    }

    private String hentMobiltelefonnummerRest(String ident) {
        DigitalKontaktinfoBolk digitalKontaktinfoBolk = consumer.hentDigitalKontaktinfo(ident);
        return digitalKontaktinfoBolk.getKontaktinfo().getMobiltelefonnummer();
    }

    private String hentMobiltelefonnummerWS(String ident) {
        DigitalKontaktinfo digitalKontaktinfo = epostService.hentInfoFraDKIF(ident);
        return digitalKontaktinfo.getMobilnummer();
    }
}
