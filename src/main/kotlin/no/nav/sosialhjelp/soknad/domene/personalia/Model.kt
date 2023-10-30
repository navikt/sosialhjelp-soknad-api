package no.nav.sosialhjelp.soknad.domene.personalia

import java.util.*

data class PersonForSoknadId (
    val personId: String,
    val soknadId: UUID,
)

data class PersonForSoknad (
    val id: PersonForSoknadId,
    val fornavn: String?,
    val mellomnavn: String? = null,
    val etternavn: String?,
    val statsborgerskap: String? = null,
    val nordiskBorger: Boolean? = null,
    val fodselsdato: String? = null
)

data class Telefonnummer (
    val soknadId: UUID,
    val kilde: SoknadKilde,
    val nummer: String
)

data class Kontonummer (
    val soknadId: UUID,
    val kilde: SoknadKilde,
    val nummer: String
)

data class AdresseForSoknadId (
    val soknadId: UUID,
    val typeAdressevalg: AdresseValg
)
data class AdresseForSoknad (
    val id: AdresseForSoknadId,
    var adresseType: AdresseType,
    var adresse: AdresseObject
)

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
