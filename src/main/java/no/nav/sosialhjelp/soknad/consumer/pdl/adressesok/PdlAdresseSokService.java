package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseSokHit;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.Criteria;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.FieldName;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.Paging;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.VegadresseDto;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslag;
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
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.AdresseHelper.formatterKommunenavn;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.FieldName.VEGADRESSE_ADRESSENAVN;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.FieldName.VEGADRESSE_HUSBOKSTAV;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.FieldName.VEGADRESSE_HUSNUMMER;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.FieldName.VEGADRESSE_POSTNUMMER;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.FieldName.VEGADRESSE_POSTSTED;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule.CONTAINS;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.SearchRule.EQUALS;
import static no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseForslagType.GATEADRESSE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlAdresseSokService {

    private static final String PAGING = "paging";
    private static final String CRITERIA = "criteria";

    private static final Logger log = getLogger(PdlAdresseSokService.class);

    private final PdlAdresseSokConsumer pdlAdresseSokConsumer;

    public PdlAdresseSokService(PdlAdresseSokConsumer pdlAdresseSokConsumer) {
        this.pdlAdresseSokConsumer = pdlAdresseSokConsumer;
    }

    public AdresseForslag getAdresseForslag(JsonGateAdresse adresse) {
        var adresseSokResult = pdlAdresseSokConsumer.getAdresseSokResult(toVariables(adresse));
        var vegadresse = resolveVegadresse(adresseSokResult.getHits());
        return toAdresseForslag(vegadresse);
    }

    private VegadresseDto resolveVegadresse(List<AdresseSokHit> hits) {
        if (hits.isEmpty()) {
            log.warn("Ingen hits i adressesok");
            throw new SosialhjelpSoknadApiException("PDL adressesok - ingen hits");
        } else if (hits.size() == 1) {
            return hits.get(0).getVegadresse();
        } else {
            var first = hits.get(0).getVegadresse();
            if (hits.stream().allMatch(hit -> relevantFieldsAreEquals(first, hit.getVegadresse()))) {
                log.info("Flere hits i adressesok, men velger første hit fra listen ettersom (kommunenummer, kommunenavn og bydelsnummer) er like.");
                return first;
            }
            log.warn("Flere ({}) hits i adressesok. Kan ikke utlede entydig kombinasjon av (kommunenummer, kommunenavn og bydelsnummer) fra alle vegadressene", hits.size());
            throw new SosialhjelpSoknadApiException("PDL adressesok - flere hits");
        }
    }

    private boolean relevantFieldsAreEquals(VegadresseDto dto1, VegadresseDto dto2) {
        if (dto1 == null || dto2 == null) return false;
        return Objects.equals(dto1.getKommunenummer(), dto2.getKommunenummer()) &&
                Objects.equals(dto1.getKommunenavn(), dto2.getKommunenavn()) &&
                Objects.equals(dto1.getBydelsnummer(), dto2.getBydelsnummer());
    }

    private String bydelsnummerOrKommunenummer(VegadresseDto vegadresse) {
        if (vegadresse.getBydelsnummer() != null) {
            return vegadresse.getBydelsnummer();
        }
        return vegadresse.getKommunenummer();
    }

    private Map<String, Object> toVariables(JsonGateAdresse adresse) {
        var variables = new HashMap<String, Object>();
        variables.put(PAGING, new Paging(1, 30, emptyList()));

        if (adresse == null) {
            throw new IllegalArgumentException("kan ikke soke uten adresse");
        }
        variables.put(CRITERIA, toCriteriaList(adresse));
        return variables;
    }

    private List<Criteria> toCriteriaList(JsonGateAdresse adresse) {
        var criteriaList = new ArrayList<Criteria>();
        if (isNotEmpty(adresse.getGatenavn())) {
            criteriaList.add(criteria(VEGADRESSE_ADRESSENAVN, CONTAINS, adresse.getGatenavn()));
        }
        if (isNotEmpty(adresse.getHusnummer())) {
            criteriaList.add(criteria(VEGADRESSE_HUSNUMMER, EQUALS, adresse.getHusnummer()));
        }
        if (isNotEmpty(adresse.getHusbokstav())) {
            criteriaList.add(criteria(VEGADRESSE_HUSBOKSTAV, EQUALS, adresse.getHusbokstav()));
        }
        if (isNotEmpty(adresse.getPostnummer())) {
            criteriaList.add(criteria(VEGADRESSE_POSTNUMMER, EQUALS, adresse.getPostnummer()));
        }
        if (isNotEmpty(adresse.getPoststed())) {
            criteriaList.add(criteria(VEGADRESSE_POSTSTED, CONTAINS, adresse.getPoststed()));
        }
        return criteriaList;
    }

    private Criteria criteria(FieldName fieldName, SearchRule searchRule, String value) {
        return new Criteria.Builder()
                .withFieldName(fieldName)
                .withSearchRule(searchRule, value)
                .build();
    }

    private AdresseForslag toAdresseForslag(VegadresseDto vegadresseDto) {
        var kommunenavnFormattert = formatterKommunenavn(vegadresseDto.getKommunenavn());
        var adresse = new AdresseForslag();
        adresse.adresse = vegadresseDto.getAdressenavn();
        adresse.husnummer = vegadresseDto.getHusnummer().toString();
        adresse.husbokstav = vegadresseDto.getHusbokstav();
        adresse.kommunenummer = vegadresseDto.getKommunenummer();
        adresse.kommunenavn = KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(vegadresseDto.getKommunenummer(), kommunenavnFormattert);
        adresse.postnummer = vegadresseDto.getPostnummer();
        adresse.poststed = vegadresseDto.getPoststed();
        adresse.geografiskTilknytning = bydelsnummerOrKommunenummer(vegadresseDto);
        adresse.type = GATEADRESSE;
        return adresse;
    }

}
