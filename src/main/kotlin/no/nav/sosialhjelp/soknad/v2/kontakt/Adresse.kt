package no.nav.sosialhjelp.soknad.v2.kontakt

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = VegAdresse::class, name = "VegAdresse"),
    JsonSubTypes.Type(value = MatrikkelAdresse::class, name = "MatrikkelAdresse"),
    JsonSubTypes.Type(value = PostboksAdresse::class, name = "PostboksAdresse"),
    JsonSubTypes.Type(value = UstrukturertAdresse::class, name = "UstrukturertAdresse"),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "VegAdresse", schema = VegAdresse::class),
        DiscriminatorMapping(value = "MatrikkelAdresse", schema = MatrikkelAdresse::class),
        DiscriminatorMapping(value = "PostboksAdresse", schema = PostboksAdresse::class),
        DiscriminatorMapping(value = "UstrukturertAdresse", schema = UstrukturertAdresse::class),
    ],
    subTypes = [VegAdresse::class, MatrikkelAdresse::class, PostboksAdresse::class, UstrukturertAdresse::class],
)
sealed interface Adresse

data class VegAdresse(
    val landkode: String = "NOR",
    val kommunenummer: String? = null,
    val adresselinjer: List<String> = emptyList(),
    val bolignummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val gatenavn: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null,
) : Adresse,
    AdresseInput

data class MatrikkelAdresse(
    val kommunenummer: String,
    val gaardsnummer: String,
    val bruksnummer: String,
    val festenummer: String? = null,
    val seksjonsnummer: String? = null,
    val undernummer: String? = null,
) : Adresse,
    AdresseInput

data class PostboksAdresse(
    val postboks: String,
    val postnummer: String,
    val poststed: String,
) : Adresse

data class UstrukturertAdresse(
    val adresse: List<String>,
) : Adresse

private val mapper = jacksonObjectMapper()

@WritingConverter
object AdresseToJsonConverter : Converter<Adresse, String> {
    override fun convert(source: Adresse): String = mapper.writeValueAsString(source)
}

@ReadingConverter
object JsonToAdresseConverter : Converter<String, Adresse> {
    //    override fun convert(source: String): Adresse = JsonToAdresseMapper.map(source)
    override fun convert(source: String): Adresse = jacksonObjectMapper().readValue(source)
}
