package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
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
sealed interface OpplysningType {
    // denne må hete `name` for pga enum.name
    val name: String
    val group: VedleggGruppe
}

enum class AnnenDokumentasjonType : OpplysningType {
    SKATTEMELDING,
    SAMVARSAVTALE,
    OPPHOLDSTILLATELSE,
    HUSLEIEKONTRAKT,
    HUSLEIEKONTRAKT_KOMMUNAL,
    BEHOV, ;

    override val group: VedleggGruppe get() = VedleggGruppe.GenerelleVedlegg
}

sealed interface OkonomiOpplysningType : OpplysningType {
    val dokumentasjonForventet: Boolean
}

enum class InntektType(
    override val dokumentasjonForventet: Boolean,
) : OkonomiOpplysningType {
    // * * * JsonOkonomioversiktInntekt * * *
    // bruker
    BARNEBIDRAG_MOTTAR(dokumentasjonForventet = true),
    STUDIELAN_INNTEKT(dokumentasjonForventet = true),

    // register - arbeidsforhold
    JOBB(dokumentasjonForventet = true),

    // * * * JsonOkonomiopplysningUtbetaling * * *
    // Andre inntekter
    UTBETALING_FORSIKRING(dokumentasjonForventet = true),
    UTBETALING_UTBYTTE(dokumentasjonForventet = true),
    UTBETALING_SALG(dokumentasjonForventet = true),
    UTBETALING_ANNET(dokumentasjonForventet = true),

    // register - arbeidsforhold
    SLUTTOPPGJOER(dokumentasjonForventet = true),

    // register
    UTBETALING_NAVYTELSE(dokumentasjonForventet = false),

    UTBETALING_SKATTEETATEN(dokumentasjonForventet = false),
    UTBETALING_HUSBANKEN(dokumentasjonForventet = false),
    ;

    override val group: VedleggGruppe
        get() =
            when (this) {
                STUDIELAN_INNTEKT, SLUTTOPPGJOER, JOBB -> VedleggGruppe.Arbeid
                BARNEBIDRAG_MOTTAR -> VedleggGruppe.Familie
                else -> VedleggGruppe.Inntekt
            }
}

enum class UtgiftType(
    override val dokumentasjonForventet: Boolean,
) : OkonomiOpplysningType {
    // * * * JsonOkonomiopplysningUtgift * * *
    // boutgifter
    UTGIFTER_ANNET_BO(dokumentasjonForventet = true),
    UTGIFTER_KOMMUNAL_AVGIFT(dokumentasjonForventet = true),
    UTGIFTER_OPPVARMING(dokumentasjonForventet = true),
    UTGIFTER_STROM(dokumentasjonForventet = true),

    // barneutgifter
    UTGIFTER_BARN_TANNREGULERING(dokumentasjonForventet = true),
    UTGIFTER_BARN_FRITIDSAKTIVITETER(dokumentasjonForventet = true),
    UTGIFTER_ANNET_BARN(dokumentasjonForventet = true),

    UTGIFTER_ANDRE_UTGIFTER(dokumentasjonForventet = true),

    // * * * JsonOkonomioversiktUtgift * * *
    BARNEBIDRAG_BETALER(dokumentasjonForventet = true),

    // barneutgifter
    UTGIFTER_SFO(dokumentasjonForventet = true),
    UTGIFTER_BARNEHAGE(dokumentasjonForventet = true),

    // boutgift
    UTGIFTER_HUSLEIE(dokumentasjonForventet = true),

    // boutgifter
    // felles håndtering av renter og avdrag fordi de knyttes sammen
    UTGIFTER_BOLIGLAN(dokumentasjonForventet = true),

    UTGIFTER_BOLIGLAN_AVDRAG(dokumentasjonForventet = true),
    UTGIFTER_BOLIGLAN_RENTER(dokumentasjonForventet = false),
    ;

    override val group: VedleggGruppe get() =
        when (this) {
            UTGIFTER_HUSLEIE, UTGIFTER_ANNET_BO -> VedleggGruppe.Bosituasjon
            BARNEBIDRAG_BETALER -> VedleggGruppe.Familie
            UTGIFTER_ANDRE_UTGIFTER -> VedleggGruppe.AndreUtgifter
            else -> VedleggGruppe.Utgifter
        }
}

enum class FormueType(
    override val dokumentasjonForventet: Boolean,
) : OkonomiOpplysningType {
    FORMUE_BRUKSKONTO(dokumentasjonForventet = true),
    FORMUE_BSU(dokumentasjonForventet = true),
    FORMUE_LIVSFORSIKRING(dokumentasjonForventet = true),
    FORMUE_SPAREKONTO(dokumentasjonForventet = true),
    FORMUE_VERDIPAPIRER(dokumentasjonForventet = true),
    FORMUE_ANNET(dokumentasjonForventet = true),
    VERDI_BOLIG(dokumentasjonForventet = false),
    VERDI_CAMPINGVOGN(dokumentasjonForventet = false),
    VERDI_KJORETOY(dokumentasjonForventet = false),
    VERDI_FRITIDSEIENDOM(dokumentasjonForventet = false),
    VERDI_ANNET(dokumentasjonForventet = false),
    ;

    override val group: VedleggGruppe get() = VedleggGruppe.Inntekt
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
