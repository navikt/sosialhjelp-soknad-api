package no.nav.sosialhjelp.soknad.v2.adresse

abstract class Adresse

data class MatrikkelAdresse(
    val kommunenummer: String? = null,
    val gaardsnummer: String? = null,
    val bruksnummer: String? = null,
    val festenummer: String? = "0",
    val seksjonsnummer: String? = "0",
    val undernummer: String? = "0"
) : Adresse()

data class VegAdresse(
    val landkode: String = "NO",
    val kommunenummer: String? = null,
    val adresselinjer: List<String> = emptyList(),
    val bolignummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val gatenavn: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null
) : Adresse()

data class PostboksAdresse(
    val postboks: String,
    val postnummer: String,
    val poststed: String
) : Adresse()

data class UstrukturertAdresse(
    val adresse: List<String>
) : Adresse()