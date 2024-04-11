package no.nav.sosialhjelp.soknad.adressesok.sok

data class Sokedata(
    val adresse: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val kommunenummer: String? = null,
)
