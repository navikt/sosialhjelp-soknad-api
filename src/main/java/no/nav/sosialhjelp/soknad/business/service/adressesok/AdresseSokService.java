package no.nav.sosialhjelp.soknad.business.service.adressesok;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;

import java.util.Collections;
import java.util.List;


public class AdresseSokService {

    private final PdlAdresseSokService pdlAdresseSokService;

    public AdresseSokService(PdlAdresseSokService pdlAdresseSokService) {
        this.pdlAdresseSokService = pdlAdresseSokService;
    }

    public List<AdresseForslag> sokEtterAdresser(String sok) {
        return pdlAdresseSokService.sokEtterAdresser(sok);
    }

    public List<AdresseForslag> finnAdresseFraSoknad(final JsonPersonalia personalia, String valg) {
        var adresse = hentValgtAdresse(personalia, valg);
        return getAdresseForslagFraPDL(adresse);
    }

    private JsonAdresse hentValgtAdresse(JsonPersonalia personalia, String valg) {
        if (valg == null) {
            return null;
        }
        switch (valg) {
            case "folkeregistrert":
                return personalia.getFolkeregistrertAdresse();
            case "midlertidig":
            case "soknad":
                return personalia.getOppholdsadresse();
            default:
                return null;
        }
    }

    private List<AdresseForslag> getAdresseForslagFraPDL(JsonAdresse adresse) {
        if (adresse == null) {
            return Collections.emptyList();
        }
        if (JsonAdresse.Type.MATRIKKELADRESSE.equals(adresse.getType())) {
            return adresseForslagForMatrikkelAdresse((JsonMatrikkelAdresse) adresse);
        } else if (JsonAdresse.Type.GATEADRESSE.equals(adresse.getType())) {
            var adresseForslag = pdlAdresseSokService.getAdresseForslag((JsonGateAdresse) adresse);
            return Collections.singletonList(adresseForslag);
        }
        return Collections.emptyList();
    }

    private List<AdresseForslag> adresseForslagForMatrikkelAdresse(JsonMatrikkelAdresse adresse) {
        var kommunenummer = adresse.getKommunenummer();
        if (kommunenummer == null || kommunenummer.trim().equals("")) {
            return Collections.emptyList();
        }

        var adresseForslag = new AdresseForslag();
        adresseForslag.type = AdresseForslagType.MATRIKKELADRESSE;
        adresseForslag.kommunenummer = kommunenummer;

        return Collections.singletonList(adresseForslag);
    }

}
