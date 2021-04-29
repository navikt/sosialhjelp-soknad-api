package no.nav.sosialhjelp.soknad.consumer.adresse;

import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslagType.GATEADRESSE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class AdresseSokService {

    private final AdresseSokConsumer adresseSokConsumer;
    private final KodeverkService kodeverkService;

    public AdresseSokService(AdresseSokConsumer adresseSokConsumer, KodeverkService kodeverkService) {
        this.adresseSokConsumer = adresseSokConsumer;
        this.kodeverkService = kodeverkService;
    }

    public List<AdresseForslag> sokEtterAdresser(String sok) {
        if (isAddressTooShortOrNull(sok)) {
            return Collections.emptyList();
        }
        final Sokedata sokedata = AdresseStringSplitter.toSokedata(kodeverkService, sok);
        return sokEtterAdresser(sokedata);
    }

    public List<AdresseForslag> sokEtterAdresser(Sokedata sokedata) {
        if (sokedata == null || isAddressTooShortOrNull(sokedata.adresse)) {
            return Collections.emptyList();
        }

        final AdressesokRespons adressesokRespons = adresseSokConsumer.sokAdresse(sokedata);
        return adressesokRespons.adresseDataList.stream()
                .filter(isGateadresse())
                .map(AdresseSokService::toAdresseForslag) // "gateadresse" er hardkodet.
                .filter(distinkte())
                .collect(toList());
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
        adresse.type = GATEADRESSE;
        return adresse;
    }

    private static Predicate<? super AdresseData> isGateadresse() {
        return data -> !isBlank(data.adressenavn)
                && !isBlank(data.postnummer)
                && !isBlank(data.poststed)
                && !isBlank(data.gatekode);
    }

    public static boolean isAddressTooShortOrNull(String address) {
        return address == null || address.trim().length() < 2;
    }

    private static String upperCase(String s) {
        if (s == null) {
            return null;
        }
        return s.toUpperCase();
    }

    private static Predicate<AdresseForslag> distinkte() {
        Set<String> funnet = new HashSet<>();
        return a -> funnet.add(a.adresse + "|" + a.kommunenummer + "|" + a.bydel + "|" + a.gatekode);
    }

}
