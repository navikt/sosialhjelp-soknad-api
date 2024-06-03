package no.nav.sosialhjelp.soknad.v2.kontakt

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

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

private val mapper = jacksonObjectMapper()

@WritingConverter
object AdresseToJsonConverter : Converter<Adresse, String> {
    override fun convert(source: Adresse): String = mapper.writeValueAsString(source)
}

@ReadingConverter
object JsonToAdresseConverter : Converter<String, Adresse> {
    override fun convert(source: String): Adresse = JsonToAdresseMapper.map(source)
}

private object JsonToAdresseMapper {
    val adresseTyper =
        setOf(
            VegAdresse::class.java,
            MatrikkelAdresse::class.java,
            PostboksAdresse::class.java,
            UstrukturertAdresse::class.java,
        )

    fun map(json: String): Adresse {
        adresseTyper.forEach { kotlin.runCatching { return mapper.readValue(json, it) } }
        throw IllegalArgumentException("Kunne ikke mappe adresse")
    }
}
