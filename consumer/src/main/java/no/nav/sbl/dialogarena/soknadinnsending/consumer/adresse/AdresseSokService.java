package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class AdresseSokService {

    @Inject
    private AdresseSokConsumer adresseSokConsumer;

    @Inject
    private NorgService norgService;
   
    public List<AdresseForslag> sokEtterAdresser(Sokedata sokedata) {
        if (sokedata.adresse != null && sokedata.adresse.trim().length() <= 2) {
            return Collections.emptyList();
        }
        
        final AdressesokRespons adressesokRespons = adresseSokConsumer.sokAdresse(sokedata);
        return adressesokRespons.adresseDataList.stream()
                .filter(isGateadresse())
                .map(AdresseSokService::toAdresseForslag) // "gateadresse" er hardkodet.
                .filter(distinkte())
                .collect(toList());
    }
    
    public List<AdresseForslag> sokEtterNavKontor(Sokedata sokedata) {
        if (sokedata.adresse != null && sokedata.adresse.trim().length() <= 2) {
            return Collections.emptyList();
        }
        
        final AdressesokRespons adressesokRespons = adresseSokConsumer.sokAdresse(sokedata);
        return adressesokRespons.adresseDataList.stream()
                .filter(distinktGeografiskTilknytning())
                .map(AdresseSokService::toKunTilknytningAdresseForslag)
                .collect(toList());
    }

    @Cacheable("kommunesokCache")
    public List<Kommunesok> sokEtterNavEnheter(String kommunenr) {
        return sokEtterNavKontor(new AdresseSokConsumer.Sokedata().withKommunenummer(kommunenr))
                .stream().map(adresseForslag -> {
                    NavEnhet navEnhet = norgService.finnEnhetForGt(adresseForslag.geografiskTilknytning);
                    return new Kommunesok(kommunenr, adresseForslag, navEnhet);
                }).collect(Collectors.toList());
    }

    static AdresseForslag toAdresseForslag(AdresseData data) {
        final AdresseForslag adresse = new AdresseForslag();
        adresse.adresse = data.adressenavn;
        adresse.husnummer = data.husnummer;
        adresse.husbokstav = upperCase(data.husbokstav);
        adresse.kommunenummer = data.kommunenummer;
        adresse.kommunenavn = KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(data.kommunenummer, data.kommunenavn);
        adresse.postnummer = data.postnummer;
        adresse.poststed = data.poststed;
        adresse.geografiskTilknytning = data.geografiskTilknytning;
        adresse.gatekode = data.gatekode;
        adresse.bydel = data.bydel;
        adresse.type = "gateadresse";
        return adresse;
    }

    private static AdresseForslag toKunTilknytningAdresseForslag(AdresseData data) {
        final AdresseForslag adresse = new AdresseForslag();
        adresse.geografiskTilknytning = data.geografiskTilknytning;
        adresse.bydel = data.bydel;
        adresse.kommunenummer = data.kommunenummer;
        adresse.kommunenavn = data.kommunenavn;
        return adresse;
    }

    private static Predicate<? super AdresseData> isGateadresse() {
        return data -> !isBlank(data.adressenavn)
                && !isBlank(data.postnummer)
                && !isBlank(data.poststed)
                && !isBlank(data.gatekode);
    }
    
    private static String upperCase(String s) {
        if (s == null) {
            return null;
        }
        return s.toUpperCase();
    }

    private static Predicate<AdresseData> distinktGeografiskTilknytning() {
        Set<String> funnet = new HashSet<>();
        return a -> funnet.add(a.geografiskTilknytning);
    }
    
    private static Predicate<AdresseForslag> distinkte() {
        Set<String> funnet = new HashSet<>();
        return a -> funnet.add(a.adresse + "|" + a.kommunenummer + "|" + a.bydel + "|" + a.gatekode);
    }

    public class Kommunesok {
        public String kommunenr;
        public AdresseForslag adresseForslag;
        public NavEnhet navEnhet;

        Kommunesok(String kommunenr, AdresseForslag adresseForslag, NavEnhet navEnhet) {
            this.kommunenr = kommunenr;
            this.adresseForslag = adresseForslag;
            this.navEnhet = navEnhet;
        }
    }

}
