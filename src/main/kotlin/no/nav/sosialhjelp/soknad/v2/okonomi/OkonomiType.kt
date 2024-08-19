package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

// TODO OpplysningType
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FormueType::class, name = "FormueType"),
    JsonSubTypes.Type(value = InntektType::class, name = "InntektType"),
    JsonSubTypes.Type(value = UtgiftType::class, name = "UtgiftType"),
)
interface OkonomiType {
    // denne må hete `name` for pga enum.name
    val name: String
    val dokumentasjonForventet: Boolean

    // TODO Er gruppe (tidligere VedleggGruppe) noe backenden skal holde styr på? - Tore
    val group: String
}

@WritingConverter
object OkonomiTypeToStringConverter : Converter<OkonomiType, String> {
    override fun convert(source: OkonomiType): String = source.name
}

@ReadingConverter
object StringToOkonomiTypeConverter : Converter<String, OkonomiType> {
    override fun convert(source: String): OkonomiType = StringToOkonomiTypeMapper.map(source)
}

// polymorphic deserialisering av enums støttes ikke ut av boksen
private object StringToOkonomiTypeMapper {
    fun map(typeString: String): OkonomiType {
        return InntektType.entries.find { it.name == typeString }
            ?: UtgiftType.entries.find { it.name == typeString }
            ?: FormueType.entries.find { it.name == typeString }
            ?: error("Kunne ikke mappe OkonomiType")
    }
}
