package no.nav.sosialhjelp.soknad.nymodell.domene.adresse

interface AdresseObject
data class MatrikkelAdresseObject(
    val kommunenummer: String? = null,
    val gaardsnummer: String? = null,
    val bruksnummer: String? = null,
    val festenummer: String? = "0",
    val seksjonsnummer: String? = "0",
    val undernummer: String? = "0"
) : AdresseObject

data class GateAdresseObject(
    val landkode: String = "NO",
    val kommunenummer: String? = null,
    val adresselinjer: List<String> = emptyList(),
    val bolignummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val gatenavn: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null
) : AdresseObject

data class PostboksAdresseObject(
    val postboks: String,
    val postnummer: String,
    val poststed: String
) : AdresseObject

data class UstrukturertAdresseObject(
    val adresse: List<String>
) : AdresseObject
