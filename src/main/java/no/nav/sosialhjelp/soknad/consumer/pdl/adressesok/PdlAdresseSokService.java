package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.Criteria;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.Paging;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule.CONTAINS;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule.EQUALS;
import static no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslagType.GATEADRESSE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class PdlAdresseSokService {

    private final PdlConsumer pdlConsumer;

    public PdlAdresseSokService(PdlConsumer pdlConsumer) {
        this.pdlConsumer = pdlConsumer;
    }

    public String getGeografiskTilknytning(AdresseSokConsumer.Sokedata sokedata) {
        var adresseSokResult = pdlConsumer.getAdresseSokResult(toVariables(sokedata));
        if (adresseSokResult.getHits() == null || adresseSokResult.getHits().isEmpty()) {
            throw new RuntimeException("ingen hits i adressesok");
        }
        if (adresseSokResult.getHits().size() > 1) {
            throw new RuntimeException("For mange hits i adressesok");
        }
        var hit = adresseSokResult.getHits().get(0);

        return bydelsnummerOrKommunenummer(hit.getVegadresse());
    }

    public AdresseForslag getAdresseForslag(AdresseSokConsumer.Sokedata sokedata) {
        var adresseSokResult = pdlConsumer.getAdresseSokResult(toVariables(sokedata));
        if (adresseSokResult.getHits() == null || adresseSokResult.getHits().isEmpty()) {
            throw new RuntimeException("ingen hits i adressesok");
        }
        if (adresseSokResult.getHits().size() > 1) {
            throw new RuntimeException("For mange hits i adressesok");
        }
        var vegadresse = adresseSokResult.getHits().get(0).getVegadresse();
        return toAdresseForslag(vegadresse);
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
