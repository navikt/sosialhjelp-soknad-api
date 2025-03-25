package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

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
interface OpplysningType {
    // denne må hete `name` for pga enum.name
    val name: String
    val dokumentasjonForventet: Boolean?
    val group: VedleggGruppe
}

@WritingConverter
object OpplysningTypeToStringConverter : Converter<OpplysningType, String> {
    override fun convert(source: OpplysningType): String = source.name
}

@ReadingConverter
object StringToOpplysningTypeConverter : Converter<String, OpplysningType> {
    override fun convert(source: String): OpplysningType = StringToOpplysningTypeMapper.map(source)
}

// polymorphic deserialisering av enums støttes ikke ut av boksen
private object StringToOpplysningTypeMapper {
    private val opplysningTypes: List<OpplysningType> =
        mutableListOf<OpplysningType>().apply {
            addAll(InntektType.entries)
            addAll(UtgiftType.entries)
            addAll(FormueType.entries)
            addAll(AnnenDokumentasjonType.entries)
        }

    fun map(typeString: String): OpplysningType {
        if (typeString == "UTGIFTER_HUSLEIE_KOMMUNAL") return UtgiftType.UTGIFTER_HUSLEIE

        return opplysningTypes.find { it.name == typeString }
            ?: error("Kunne ikke mappe til OpplysningType: $typeString")
    }
}
