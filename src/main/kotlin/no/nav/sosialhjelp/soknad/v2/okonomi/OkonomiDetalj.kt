package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.LocalDate

/**
 * Inntekter, Utgifter, Formue og Utbetaling har veldig forskjellige "behov"
 * Tanken er å kun måtte lagre nødvendige felter for hver klasse
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Belop::class, name = "Belop"),
    JsonSubTypes.Type(value = BruttoNetto::class, name = "BruttoNetto"),
    JsonSubTypes.Type(value = Utbetaling::class, name = "Utbetaling"),
    JsonSubTypes.Type(value = UtbetalingMedKomponent::class, name = "UtbetalingMedKomponent"),
)
interface OkonomiDetalj

data class OkonomiskeDetaljer<T : OkonomiDetalj>(
    val detaljer: List<T> = emptyList(),
)

data class Belop(
    val belop: Double,
) : OkonomiDetalj

data class BruttoNetto(
    val brutto: Double? = null,
    val netto: Double? = null,
) : OkonomiDetalj

// TODO Sjekk bruk av utbetaling og om det trengs alle feltene

data class Utbetaling(
    val brutto: Double? = null,
    val netto: Double? = null,
    val belop: Double? = null,
    val skattetrekk: Double? = null,
    val andreTrekk: Double? = null,
    // TODO Skal være YYYY-MM-DD
    val utbetalingsdato: LocalDate? = null,
    val periodeFom: LocalDate? = null,
    val periodeTom: LocalDate? = null,
    val mottaker: Mottaker? = null,
) : OkonomiDetalj

data class UtbetalingMedKomponent(
    val utbetaling: Utbetaling? = Utbetaling(),
    val komponenter: List<Komponent> = emptyList(),
) : OkonomiDetalj

data class Komponent(
    val type: String? = null,
    val belop: String? = null,
    val satsType: String? = null,
    val satsAntall: Double? = null,
    val satsBelop: Double? = null,
)

enum class Mottaker {
    HUSSTAND,
    KOMMUNE,
}

private val mapper =
    jacksonObjectMapper().apply {
        registerModules(JavaTimeModule())
    }

@WritingConverter
class OkonomiskeDetaljerToStringConverter<T : OkonomiDetalj> : Converter<OkonomiskeDetaljer<T>, String> {
    override fun convert(source: OkonomiskeDetaljer<T>): String = mapper.writeValueAsString(source)
}

@ReadingConverter
class StringToOkonomiskeDetaljerConverter<T : OkonomiDetalj> : Converter<String, OkonomiskeDetaljer<T>> {
    override fun convert(source: String): OkonomiskeDetaljer<T> = mapper.readValue(source)
}

// object OkonomiRaderMapper {
//
//    fun mapToOkonomiRad(source: String): OkonomiRader<OkonomiRad> {
// //        return mapper.readValue()
//    }
//
// }
