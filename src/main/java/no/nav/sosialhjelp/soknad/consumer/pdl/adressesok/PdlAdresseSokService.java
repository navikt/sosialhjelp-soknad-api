package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseSokHit;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.Criteria;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.Paging;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer;
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule.CONTAINS;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule.EQUALS;
import static no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslagType.GATEADRESSE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlAdresseSokService {

    private static final Logger log = getLogger(PdlAdresseSokService.class);

    private final PdlConsumer pdlConsumer;

    public PdlAdresseSokService(PdlConsumer pdlConsumer) {
        this.pdlConsumer = pdlConsumer;
    }

    public String getGeografiskTilknytning(AdresseSokConsumer.Sokedata sokedata) {
        var adresseSokResult = pdlConsumer.getAdresseSokResult(toVariables(sokedata));
        var vegadresse = resolveVegadresse(adresseSokResult.getHits());
        return bydelsnummerOrKommunenummer(vegadresse);
    }

    public AdresseForslag getAdresseForslag(AdresseSokConsumer.Sokedata sokedata) {
        var adresseSokResult = pdlConsumer.getAdresseSokResult(toVariables(sokedata));
        var vegadresse = resolveVegadresse(adresseSokResult.getHits());
        return toAdresseForslag(vegadresse);
    }

    private AdresseDto resolveVegadresse(List<AdresseSokHit> hits) {
        if (hits.isEmpty()) {
            log.warn("Ingen hits i adressesok");
            throw new SosialhjelpSoknadApiException("PDL adressesok - ingen hits");
        } else if (hits.size() == 1) {
            return hits.get(0).getVegadresse();
        } else {
            var first = hits.get(0).getVegadresse();
            if (hits.stream().allMatch(hit -> relevantFieldsAreEquals(first, hit.getVegadresse()))) {
                log.info("Flere hits i adressesok, men velger første hit fra listen ettersom (kommunnummer, kommunenavn og bydelsenummer) er like.");
                return first;
            }
            log.warn("Flere ({}) hits i adressesok. Kan ikke utlede entydig kombinasjon av (kommunenummer, kommunenavn og bydelsnummer) fra alle vegadressene", hits.size());
            throw new SosialhjelpSoknadApiException("PDL adressesok - flere hits");
        }
    }

    private boolean relevantFieldsAreEquals(AdresseDto dto1, AdresseDto dto2) {
        if (dto1 == null || dto2 == null) return false;
        return Objects.equals(dto1.getKommunenummer(), dto2.getKommunenummer()) &&
                Objects.equals(dto1.getKommunenavn(), dto2.getKommunenavn()) &&
                Objects.equals(dto1.getBydelsnummer(), dto2.getBydelsnummer());
    }

    private String bydelsnummerOrKommunenummer(AdresseDto vegadresse) {
        if (vegadresse.getBydelsnummer() != null) {
            return vegadresse.getBydelsnummer();
        }
        return vegadresse.getKommunenummer();
    }

    private Map<String, Object> toVariables(AdresseSokConsumer.Sokedata sokedata) {
        var variables = new HashMap<String, Object>();
        variables.put("paging", new Paging(1, 30, emptyList()));

        if (sokedata == null) {
            throw new IllegalArgumentException("kan ikke soke uten sokedata");
        }
        variables.put("criteria", toCriteriaList(sokedata));
        return variables;
    }

    private List<Criteria> toCriteriaList(AdresseSokConsumer.Sokedata sokedata) {
        var criteriaList = new ArrayList<Criteria>();
        if (isNotEmpty(sokedata.adresse)) {
            criteriaList.add(criteria("adressenavn", CONTAINS, sokedata.adresse));
        }
        if (isNotEmpty(sokedata.husnummer)) {
            criteriaList.add(criteria("husnummer", EQUALS, sokedata.husnummer));
        }
        if (isNotEmpty(sokedata.husbokstav)) {
            criteriaList.add(criteria("hustbokstav", EQUALS, sokedata.husbokstav));
        }
        if (isNotEmpty(sokedata.poststed)) {
            criteriaList.add(criteria("poststed", EQUALS, sokedata.poststed));
        }
        return criteriaList;
    }

    private Criteria criteria(String fieldName, SearchRule searchRule, String value) {
        return new Criteria.Builder()
                .withFieldName(fieldName)
                .withSearchRule(searchRule, value)
                .build();
    }

    private AdresseForslag toAdresseForslag(AdresseDto adresseDto) {
        var adresse = new AdresseForslag();
        adresse.adresse = adresseDto.getAdressenavn();
        adresse.husnummer = adresseDto.getHusnummer().toString();
        adresse.husbokstav = adresseDto.getHusbokstav();
        adresse.kommunenummer = adresseDto.getKommunenummer();
        adresse.kommunenavn = KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(adresseDto.getKommunenummer(), adresseDto.getKommunenavn());
        adresse.postnummer = adresseDto.getPostnummer();
        adresse.poststed = adresseDto.getPoststed();
        adresse.geografiskTilknytning = bydelsnummerOrKommunenummer(adresseDto);
        adresse.type = GATEADRESSE;
        return adresse;
    }

}
