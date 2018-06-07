package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Component
public class SoknadsmottakerService {
    private static final Logger logger = LoggerFactory.getLogger(SoknadsmottakerService.class);

    @Inject
    private AdresseSokService adresseSokService;

    public AdresseForslag finnAdresseFraSoknad(final WebSoknad webSoknad) {
        final Faktum adresseFaktum = hentAdresseFaktum(webSoknad);
        if (adresseFaktum == null) {
            return null;
        }
        final Map<String, String> adresse = adresseFaktum.getProperties();
        if (adresse == null || adresse.isEmpty()) {
            return null;
        }

        final String type = adresse.get("type");
        if (type == null) {
            return null;
        }
        if ("matrikkeladresse".equals(type)) {
            final String kommunenummer = adresse.get("kommunenummer");
            if (kommunenummer == null) {
                return null;
            }
            final AdresseForslag adresseForslag = new AdresseForslag();
            adresseForslag.kommunenummer = kommunenummer;
            adresseForslag.geografiskTilknytning = kommunenummer;
            return adresseForslag;
        } else {
            final String sokestreng = lagSokestreng(adresse);
            final List<AdresseForslag> adresser = adresseSokService.sokEtterAdresser(sokestreng);
            if (adresser == null || adresser.size() != 1) {
                logger.warn("Fant ikke entydig adresse for søkekriterier {}", sokestreng);
                return null;
            }
            return adresser.get(0);
        }
    }

    Faktum hentAdresseFaktum(final WebSoknad webSoknad) {
        if (webSoknad == null) {
            logger.warn("Søknaden er null");
            return null;
        }
        final String adressevalg = webSoknad.getValueForFaktum("kontakt.system.oppholdsadresse.valg");
        if ("folkeregistrert".equals(adressevalg)) {
            return webSoknad.getFaktumMedKey("kontakt.system.folkeregistrert.adresse");
        } else if ("midlertidig".equals(adressevalg)) {
            return webSoknad.getFaktumMedKey("kontakt.system.adresse");
        } else if ("soknad".equals(adressevalg)){
            return webSoknad.getFaktumMedKey("kontakt.adresse.bruker");
        } else {
            return null;
        }
    }

    private static String emptyIfNull(String s) {
        return (s != null) ? s : "";
    }
    private String lagSokestreng(final Map<String, String> adresse) {
        // TODO: Denne metoden er midlertidig og skal fjernes før produksjon (søk bør gjøres strukturert). 

        final String gatenavn = emptyIfNull(adresse.get("gatenavn"));
        final String husnummer = emptyIfNull(adresse.get("husnummer"));
        final String husbokstav = emptyIfNull(adresse.get("husbokstav"));
        final String postnummer = emptyIfNull(adresse.get("postnummer"));
        final String poststed = emptyIfNull(adresse.get("poststed"));
        
        return ((gatenavn + " " + husnummer + husbokstav).trim() + ", " + postnummer + " " + poststed).trim();
    }
}
