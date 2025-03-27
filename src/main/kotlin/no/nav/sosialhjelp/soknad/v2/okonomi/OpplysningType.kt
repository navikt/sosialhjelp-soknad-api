package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
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
    JsonSubTypes.Type(value = AnnenDokumentasjonType::class, name = "AnnenDokumentasjonType"),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "FormueType", schema = FormueType::class),
        DiscriminatorMapping(value = "InntektType", schema = InntektType::class),
        DiscriminatorMapping(value = "UtgiftType", schema = UtgiftType::class),
        DiscriminatorMapping(value = "AnnenDokumentasjonType", schema = AnnenDokumentasjonType::class),
    ],
    subTypes = [InntektType::class, FormueType::class, UtgiftType::class, AnnenDokumentasjonType::class],
)
interface OpplysningType {
    // denne må hete `name` for pga enum.name
    val name: String
    val dokumentasjonForventet: Boolean?

    // TODO Er gruppe (tidligere VedleggGruppe) noe backenden skal holde styr på? - Tore
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
        // TODO Midlertidig manuell mapping pga eksisterende data med feil type
        if (typeString == "UTGIFTER_HUSLEIE_KOMMUNAL") return UtgiftType.UTGIFTER_HUSLEIE

        return opplysningTypes.find { it.name == typeString }
            ?: error("Kunne ikke mappe til OpplysningType: $typeString")
    }
}
