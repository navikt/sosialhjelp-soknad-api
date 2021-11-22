package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto;
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto;
import no.nav.sosialhjelp.soknad.adressesok.sok.Criteria;
import no.nav.sosialhjelp.soknad.adressesok.sok.Direction;
import no.nav.sosialhjelp.soknad.adressesok.sok.FieldName;
import no.nav.sosialhjelp.soknad.adressesok.sok.Paging;
import no.nav.sosialhjelp.soknad.adressesok.sok.SearchRule;
import no.nav.sosialhjelp.soknad.adressesok.sok.SortBy;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseStringSplitter;
import no.nav.sosialhjelp.soknad.business.service.adressesok.Sokedata;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.adressesok.AdressesokUtilsKt.formatterKommunenavn;
import static no.nav.sosialhjelp.soknad.adressesok.sok.FieldName.VEGADRESSE_ADRESSENAVN;
import static no.nav.sosialhjelp.soknad.adressesok.sok.FieldName.VEGADRESSE_HUSBOKSTAV;
import static no.nav.sosialhjelp.soknad.adressesok.sok.FieldName.VEGADRESSE_HUSNUMMER;
import static no.nav.sosialhjelp.soknad.adressesok.sok.FieldName.VEGADRESSE_KOMMUNENUMMER;
import static no.nav.sosialhjelp.soknad.adressesok.sok.FieldName.VEGADRESSE_POSTNUMMER;
import static no.nav.sosialhjelp.soknad.adressesok.sok.FieldName.VEGADRESSE_POSTSTED;
import static no.nav.sosialhjelp.soknad.adressesok.sok.SearchRule.CONTAINS;
import static no.nav.sosialhjelp.soknad.adressesok.sok.SearchRule.EQUALS;
import static no.nav.sosialhjelp.soknad.adressesok.sok.SearchRule.WILDCARD;
import static no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslagType.GATEADRESSE;
import static no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseStringSplitter.isAddressTooShortOrNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlAdresseSokService {

    private static final String PAGING = "paging";
    private static final String CRITERIA = "criteria";

    private static final Logger log = getLogger(PdlAdresseSokService.class);
    public static final String WILDCARD_SUFFIX = "*";

    private final PdlAdresseSokConsumer pdlAdresseSokConsumer;
    private final KodeverkService kodeverkService;

    public PdlAdresseSokService(
            PdlAdresseSokConsumer pdlAdresseSokConsumer,
            KodeverkService kodeverkService
    ) {
        this.pdlAdresseSokConsumer = pdlAdresseSokConsumer;
        this.kodeverkService = kodeverkService;
    }

    public AdresseForslag getAdresseForslag(JsonGateAdresse adresse) {
        var adresseSokResult = pdlAdresseSokConsumer.getAdresseSokResult(toVariables(adresse));
        var vegadresse = resolveVegadresse(adresseSokResult.getHits());
        return toAdresseForslag(vegadresse);
    }

    public List<AdresseForslag> sokEtterAdresser(String sok) {
        if (isAddressTooShortOrNull(sok)) {
            return Collections.emptyList();
        }

        var sokedata = AdresseStringSplitter.toSokedata(kodeverkService, sok);

        return getAdresser(sokedata).stream()
                .map(this::toAdresseForslag)
                .collect(Collectors.toList());
    }

    public List<VegadresseDto> getAdresser(Sokedata sokedata) {
        if (sokedata == null || isAddressTooShortOrNull(sokedata.adresse)) {
            return Collections.emptyList();
        }
        var adresseSokResult = pdlAdresseSokConsumer.getAdresseSokResult(toVariablesForFritekstSok(sokedata));
        return adresseSokResult.getHits().stream()
                .map(AdressesokHitDto::getVegadresse)
                .collect(Collectors.toList());
    }

    private VegadresseDto resolveVegadresse(List<AdressesokHitDto> hits) {
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

    private Map<String, Object> toVariablesForFritekstSok(Sokedata sokedata) {
        var variables = new HashMap<String, Object>();
        variables.put(PAGING, new Paging(1, 30, singletonList(new SortBy(VEGADRESSE_HUSNUMMER.getValue(), Direction.ASC))));

        if (sokedata == null) {
            throw new IllegalArgumentException("kan ikke soke uten sokedata");
        }

        variables.put(CRITERIA, toCriteriaListForFritekstSok(sokedata));
        return variables;
    }

    private List<Criteria> toCriteriaListForFritekstSok(Sokedata sokedata) {
        var criteriaList = new ArrayList<Criteria>();
        if (isNotEmpty(sokedata.adresse)) {
            if (sokedata.adresse.length() < 3) {
                criteriaList.add(criteria(VEGADRESSE_ADRESSENAVN, EQUALS, sokedata.adresse));
            } else {
                criteriaList.add(criteria(VEGADRESSE_ADRESSENAVN, WILDCARD, sokedata.adresse));
            }
        }
        if (isNotEmpty(sokedata.husnummer)) {
            criteriaList.add(criteria(VEGADRESSE_HUSNUMMER, WILDCARD, sokedata.husnummer));
        }
        if (isNotEmpty(sokedata.husbokstav)) {
            criteriaList.add(criteria(VEGADRESSE_HUSBOKSTAV, EQUALS, sokedata.husbokstav));
        }
        if (isNotEmpty(sokedata.postnummer)) {
            criteriaList.add(criteria(VEGADRESSE_POSTNUMMER, EQUALS, sokedata.postnummer));
        }
        if (isNotEmpty(sokedata.poststed)) {
            criteriaList.add(criteria(VEGADRESSE_POSTSTED, WILDCARD, sokedata.poststed));
        }
        if (isNotEmpty(sokedata.kommunenummer)) {
            criteriaList.add(criteria(VEGADRESSE_KOMMUNENUMMER, EQUALS, sokedata.kommunenummer));
        }
        return criteriaList;
    }

    private Criteria criteria(FieldName fieldName, SearchRule searchRule, String value) {
        if (WILDCARD.equals(searchRule)) {
            value += WILDCARD_SUFFIX;
        }
        return new Criteria(fieldName, searchRule, value);
    }

    private AdresseForslag toAdresseForslag(VegadresseDto vegadresseDto) {
        var kommunenavnFormattert = formatterKommunenavn(vegadresseDto.getKommunenavn());
        var adresse = new AdresseForslag();
        adresse.adresse = vegadresseDto.getAdressenavn();
        adresse.husnummer = vegadresseDto.getHusnummer() == null ? null : vegadresseDto.getHusnummer().toString();
        adresse.husbokstav = vegadresseDto.getHusbokstav();
        adresse.kommunenummer = vegadresseDto.getKommunenummer();
        adresse.kommunenavn = KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(vegadresseDto.getKommunenummer(), kommunenavnFormattert);
        adresse.postnummer = vegadresseDto.getPostnummer();
        adresse.poststed = vegadresseDto.getPoststed();
        adresse.geografiskTilknytning = bydelsnummerOrKommunenummer(vegadresseDto);
        adresse.type = GATEADRESSE;
        return adresse;
    }

    private String bydelsnummerOrKommunenummer(VegadresseDto vegadresse) {
        if (vegadresse.getBydelsnummer() != null) {
            return vegadresse.getBydelsnummer();
        }
        return vegadresse.getKommunenummer();
    }

}
