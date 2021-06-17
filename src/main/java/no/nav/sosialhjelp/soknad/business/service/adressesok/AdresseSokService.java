package no.nav.sosialhjelp.soknad.business.service.adressesok;

import no.finn.unleash.Unleash;
import no.nav.sosialhjelp.soknad.consumer.adresse.TpsAdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class AdresseSokService {

    public static final String FEATURE_PDL_ADRESSESOK_ENABLED = "sosialhjelp.soknad.pdl-adressesok-enabled";
    private static final Logger log = getLogger(AdresseSokService.class);

    private final Unleash unleash;
    private final PdlAdresseSokService pdlAdresseSokService;
    private final TpsAdresseSokService tpsAdresseSokService;

    public AdresseSokService(
            TpsAdresseSokService tpsAdresseSokService,
            PdlAdresseSokService pdlAdresseSokService,
            Unleash unleash
    ) {
        this.tpsAdresseSokService = tpsAdresseSokService;
        this.pdlAdresseSokService = pdlAdresseSokService;
        this.unleash = unleash;
    }

    public List<AdresseForslag> sokEtterAdresser(String sok) {
        if (unleash.isEnabled(FEATURE_PDL_ADRESSESOK_ENABLED, false)) {
            try {
                return sokEtterAdresserPDL(sok);
            } catch (Exception e) {
                log.warn("Noe uventet feilet ved kall mot PDL adressesøk -> Fallback mot TPS");
                return sokEtterAdresserTPS(sok);
            }
        }
        return sokEtterAdresserTPS(sok);
    }

    private List<AdresseForslag> sokEtterAdresserPDL(String sok) {
        return pdlAdresseSokService.sokEtterAdresser(sok);
    }

    private List<AdresseForslag> sokEtterAdresserTPS(String sok) {
        return tpsAdresseSokService.sokEtterAdresser(sok);
    }
}
