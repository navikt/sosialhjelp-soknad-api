package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Service
public class AdresseSokService {

    @Inject
    private AdresseSokConsumer adresseSokConsumer;

    public List<AdresseForslag> sokEtterAdresser(String sok) {
        if (sok == null || sok.trim().length() <= 2) {
            return null;
        }

        AdressesokRespons adressesokRespons = adresseSokConsumer.sokAdresse(sok);

        List<AdresseForslag> forslag = adressesokRespons.adresseDataList.stream()
                .map(data -> {
                    AdresseForslag adresse = new AdresseForslag();
                    adresse.adresse = data.adressenavn;
                    adresse.husnummer = data.husnummer;
                    adresse.husbokstav = upperCase(data.husbokstav);
                    adresse.kommunenummer = data.kommunenummer;
                    adresse.kommunenavn = data.kommunenavn;
                    adresse.postnummer = data.postnummer;
                    adresse.poststed = data.poststed;
                    adresse.geografiskTilknytning = data.geografiskTilknytning;
                    adresse.gatekode = data.gatekode;
                    adresse.bydel = data.bydel;
                    adresse.type = "gateadresse"; // TODO: Legge til støtte for filtering / andre typer.
                    return adresse;
                })
                .filter(distinkte())
                .collect(toList());

        return forslag;
    }

    private String upperCase(String s) {
        if (s == null) {
            return null;
        }
        return s.toUpperCase();
    }

    private Predicate<AdresseForslag> distinkte() {
        Set<String> funnet = new HashSet<>();
        return a -> funnet.add(a.adresse + "|" + a.kommunenummer + "|" + a.bydel + "|" + a.gatekode);
    }

}
