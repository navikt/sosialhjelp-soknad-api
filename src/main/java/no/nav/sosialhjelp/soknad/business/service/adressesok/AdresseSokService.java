package no.nav.sosialhjelp.soknad.business.service.adressesok;

import no.finn.unleash.Unleash;
import no.nav.sosialhjelp.soknad.consumer.adresse.TpsAdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;

import java.util.List;

public class AdresseSokService {

    private static final String FEATURE_PDL_ADRESSESOK_ENABLED = "sosialhjelp.soknad.pdl-adressesok-enabled";

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
            return sokEtterAdresserPDL(sok);
        }
        return sokEtterAdresserTPS(sok);
    }

    private List<AdresseForslag> sokEtterAdresserPDL(String sok) {
        // TODO implement using pdlAdresseSokService
        return pdlAdresseSokService.getAdresseForslagList(sok);
    }

    private List<AdresseForslag> sokEtterAdresserTPS(String sok) {
        return tpsAdresseSokService.sokEtterAdresser(sok);
    }
}
