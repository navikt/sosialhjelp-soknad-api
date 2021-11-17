package no.nav.sosialhjelp.soknad.adressesok.sok

data class Criteria(
    val fieldName: String,
    val searchRule: Map<String, String>
) {
    constructor(fieldName: FieldName, searchRule: SearchRule, value: String) : this(fieldName.value, mapOf(searchRule.keyName to value))
}

enum class FieldName(
    val value: String
) {
    VEGADRESSE_ADRESSENAVN("vegadresse.adressenavn"),
    VEGADRESSE_HUSNUMMER("vegadresse.husnummer"),
    VEGADRESSE_HUSBOKSTAV("vegadresse.husbokstav"),
    VEGADRESSE_POSTNUMMER("vegadresse.postnummer"),
    VEGADRESSE_POSTSTED("vegadresse.poststed"),
    VEGADRESSE_KOMMUNENUMMER("vegadresse.kommunenummer");
}

enum class SearchRule (
    val keyName: String
){
    EQUALS("equals"),
    CONTAINS("contains"),
    FUZZY("fuzzy"),
    FROM("from"),
    WILDCARD("wildcard");
}

data class Paging(
    val pageNumber: Int,
    val resultsPerPage: Int,
    val sortBy: List<SortBy>
)

data class SortBy(
    val fieldName: String,
    val direction: Direction
)

enum class Direction {
    ASC, DESC
}
