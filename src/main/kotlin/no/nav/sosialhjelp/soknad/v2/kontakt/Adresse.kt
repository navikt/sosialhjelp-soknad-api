package no.nav.sosialhjelp.soknad.v2.kontakt

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
)
@JsonSubTypes(
    JsonSubTypes.Type(VegAdresse::class),
    JsonSubTypes.Type(MatrikkelAdresse::class),
    JsonSubTypes.Type(PostboksAdresse::class),
    JsonSubTypes.Type(UstrukturertAdresse::class),
)
abstract class Adresse

data class VegAdresse(
    val landkode: String = "NO",
    val kommunenummer: String? = null,
    val adresselinjer: List<String> = emptyList(),
    val bolignummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val gatenavn: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null,
) : Adresse()

data class MatrikkelAdresse(
    val kommunenummer: String,
    val gaardsnummer: String,
    val bruksnummer: String,
    val festenummer: String? = null,
    val seksjonsnummer: String? = null,
    val undernummer: String? = null,
) : Adresse()

data class PostboksAdresse(
    val postboks: String,
    val postnummer: String,
    val poststed: String,
) : Adresse()

data class UstrukturertAdresse(
    val adresse: List<String>,
) : Adresse()
