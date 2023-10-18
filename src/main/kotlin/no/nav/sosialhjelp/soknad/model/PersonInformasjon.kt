package no.nav.sosialhjelp.soknad.model

import org.springframework.data.annotation.Id

data class PersonIdentifikator (
    val verdi: String
)

data class Person (
    @Id val id: PersonIdentifikator,
    val fornavn: String,
    val mellomnavn: String,
    val etternavn: String,
    val statsborgerskap: String,
    val nordiskBorger: Boolean,
)

enum class AdresseValg {
    FOLKEREGISTRERT, OPPHOLD, SOKNAD
}

interface AdresseObject
data class MatrikkelAdresseObject (
    val kommunenummer: String,
    val gaardsnummer: String,
    val bruksnummer: String,
    val festenummer: String = "0",
    val seksjonsnummer: String = "0",
    val undernummer: String = "0"
): AdresseObject

data class GateAdresseObject (
    val landkode: String = "NO",
    val kommunenummer: String,
    val adresselinjer: List<String> = emptyList(),
    val bolignummer: String? = null,
    val postnummer: String,
    val poststed: String,
    val gatenavn: String,
    val husnummer: String,
    val husbokstav: String? = null
): AdresseObject

data class PostboksAdresseObject (
    val postboks: String,
    val postnummer: String,
    val poststed: String
): AdresseObject

data class UstrukturertAdresseObject (
    val adresse: List<String>
): AdresseObject
