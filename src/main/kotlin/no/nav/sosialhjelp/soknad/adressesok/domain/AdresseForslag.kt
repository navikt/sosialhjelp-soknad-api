package no.nav.sosialhjelp.soknad.adressesok.domain

data class AdresseForslag(
    val adresse: String?,
    val husnummer: String?,
    val husbokstav: String?,
    val kommunenummer: String?,
    val kommunenavn: String?,
    val postnummer: String?,
    val poststed: String?,
    val geografiskTilknytning: String?,
    val gatekode: String? = null,
    val bydel: String? = null,
    val type: AdresseForslagType
) {
    constructor(kommunenummer: String, type: AdresseForslagType) : this(null, null, null, kommunenummer, null, null, null, null, null, null, type)
}

enum class AdresseForslagType(
    val value: String
) {
    GATEADRESSE("gateadresse"),
    MATRIKKELADRESSE("matrikkeladresse")
}
