package no.nav.sosialhjelp.soknad.adressesok

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.adressesok.sok.AdresseStringSplitter
import no.nav.sosialhjelp.soknad.adressesok.sok.AdresseStringSplitter.isAddressTooShortOrNull
import no.nav.sosialhjelp.soknad.adressesok.sok.Criteria
import no.nav.sosialhjelp.soknad.adressesok.sok.Direction
import no.nav.sosialhjelp.soknad.adressesok.sok.FieldName
import no.nav.sosialhjelp.soknad.adressesok.sok.Paging
import no.nav.sosialhjelp.soknad.adressesok.sok.SearchRule
import no.nav.sosialhjelp.soknad.adressesok.sok.Sokedata
import no.nav.sosialhjelp.soknad.adressesok.sok.SortBy
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class AdressesokService(
    private val adressesokClient: AdressesokClient,
    private val kodeverkService: KodeverkService
) {

    open fun getAdresseForslag(adresse: JsonGateAdresse?): AdresseForslag {
        val adresseSokResult = adressesokClient.getAdressesokResult(toVariables(adresse))
        val vegadresse = resolveVegadresse(adresseSokResult?.hits ?: emptyList())
        return vegadresse.toAdresseForslag()
    }

    open fun sokEtterAdresser(sokeString: String?): List<AdresseForslag> {
        if (isAddressTooShortOrNull(sokeString)) {
            return emptyList()
        }

        val sokedata = AdresseStringSplitter.toSokedata(kodeverkService, sokeString)
        return getAdresser(sokedata)
            .map { it.toAdresseForslag() }
    }

    private fun getAdresser(sokedata: Sokedata?): List<VegadresseDto> {
        if (sokedata == null || isAddressTooShortOrNull(sokedata.adresse)) {
            return emptyList()
        }
        val adressesokResult = adressesokClient.getAdressesokResult(toVariablesForFritekstSok(sokedata))
        return adressesokResult?.hits?.map { it.vegadresse } ?: emptyList()
    }

    private fun toVariables(adresse: JsonGateAdresse?): Map<String, Any> {
        val variables = HashMap<String, Any>()
        variables[PAGING] = Paging(1, 30, emptyList())

        requireNotNull(adresse) { "kan ikke soke uten adresse" }
        variables[CRITERIA] = toCriteriaList(adresse)
        return variables
    }

    private fun toCriteriaList(adresse: JsonGateAdresse): List<Criteria> {
        val criteriaList = ArrayList<Criteria>()
        if (StringUtils.isNotEmpty(adresse.gatenavn)) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_ADRESSENAVN, SearchRule.CONTAINS, adresse.gatenavn))
        }
        if (StringUtils.isNotEmpty(adresse.husnummer)) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_HUSNUMMER, SearchRule.EQUALS, adresse.husnummer))
        }
        if (StringUtils.isNotEmpty(adresse.husbokstav)) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_HUSBOKSTAV, SearchRule.EQUALS, adresse.husbokstav))
        }
        if (StringUtils.isNotEmpty(adresse.postnummer)) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_POSTNUMMER, SearchRule.EQUALS, adresse.postnummer))
        }
        if (StringUtils.isNotEmpty(adresse.poststed)) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_POSTSTED, SearchRule.CONTAINS, adresse.poststed))
        }
        return criteriaList
    }

    private fun toVariablesForFritekstSok(sokedata: Sokedata?): Map<String, Any> {
        val variables = java.util.HashMap<String, Any>()
        variables[PAGING] = Paging(1, 30, listOf(SortBy(FieldName.VEGADRESSE_HUSNUMMER.value, Direction.ASC)))
        requireNotNull(sokedata) { "kan ikke soke uten sokedata" }
        variables[CRITERIA] = toCriteriaListForFritekstSok(sokedata)
        return variables
    }

    private fun toCriteriaListForFritekstSok(sokedata: Sokedata): List<Criteria> {
        val criteriaList = java.util.ArrayList<Criteria>()
        if (sokedata.adresse != null) {
            if (sokedata.adresse.length < 3) {
                criteriaList.add(criteria(FieldName.VEGADRESSE_ADRESSENAVN, SearchRule.EQUALS, sokedata.adresse))
            } else {
                criteriaList.add(criteria(FieldName.VEGADRESSE_ADRESSENAVN, SearchRule.WILDCARD, sokedata.adresse))
            }
        }
        if (sokedata.husnummer != null) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_HUSNUMMER, SearchRule.WILDCARD, sokedata.husnummer))
        }
        if (sokedata.husbokstav != null) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_HUSBOKSTAV, SearchRule.EQUALS, sokedata.husbokstav))
        }
        if (sokedata.postnummer != null) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_POSTNUMMER, SearchRule.EQUALS, sokedata.postnummer))
        }
        if (sokedata.poststed != null) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_POSTSTED, SearchRule.WILDCARD, sokedata.poststed))
        }
        if (sokedata.kommunenummer != null) {
            criteriaList.add(criteria(FieldName.VEGADRESSE_KOMMUNENUMMER, SearchRule.EQUALS, sokedata.kommunenummer))
        }
        return criteriaList
    }

    private fun criteria(fieldName: FieldName, searchRule: SearchRule, value: String): Criteria {
        val newValue = if (SearchRule.WILDCARD == searchRule) value + WILDCARD_SUFFIX else value
        return Criteria(fieldName, searchRule, newValue)
    }

    private fun resolveVegadresse(hits: List<AdressesokHitDto>): VegadresseDto {
        return if (hits.isEmpty()) {
            log.warn("Ingen hits i adressesok")
            throw SosialhjelpSoknadApiException("PDL adressesok - ingen hits")
        } else if (hits.size == 1) {
            hits[0].vegadresse
        } else {
            val first = hits[0].vegadresse
            if (hits.all { relevantFieldsAreEquals(first, it.vegadresse) }) {
                log.info("Flere hits i adressesok, men velger første hit fra listen ettersom (kommunenummer, kommunenavn og bydelsnummer) er like.")
                return first
            }
            log.warn("Flere (${hits.size}) hits i adressesok. Kan ikke utlede entydig kombinasjon av (kommunenummer, kommunenavn og bydelsnummer) fra alle vegadressene")
            throw SosialhjelpSoknadApiException("PDL adressesok - flere hits")
        }
    }

    private fun relevantFieldsAreEquals(dto1: VegadresseDto?, dto2: VegadresseDto?): Boolean {
        return if (dto1 == null || dto2 == null) {
            false
        } else {
            dto1.kommunenummer == dto2.kommunenummer && dto1.kommunenavn == dto2.kommunenavn && dto1.bydelsnummer == dto2.bydelsnummer
        }
    }

    companion object {
        private const val PAGING = "paging"
        private const val CRITERIA = "criteria"
        private const val WILDCARD_SUFFIX = "*"

        private val log = LoggerFactory.getLogger(AdressesokService::class.java)
    }
}
