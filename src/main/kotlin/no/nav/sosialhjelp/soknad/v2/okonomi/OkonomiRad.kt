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
interface OkonomiRad

data class OkonomiRader<T : OkonomiRad>(
    val rader: List<T> = emptyList(),
)

data class Belop(
    val belop: Double,
) : OkonomiRad

data class BruttoNetto(
    val brutto: Double? = null,
    val netto: Double? = null,
) : OkonomiRad

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
) : OkonomiRad

data class UtbetalingMedKomponent(
    val utbetaling: Utbetaling? = Utbetaling(),
    val komponenter: List<Komponent> = emptyList(),
) : OkonomiRad

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
class OkonomiRaderToStringConverter<T : OkonomiRad> : Converter<OkonomiRader<T>, String> {
    override fun convert(source: OkonomiRader<T>): String = mapper.writeValueAsString(source)
}

@ReadingConverter
class StringToOkonomiRadConverter<T : OkonomiRad> : Converter<String, OkonomiRader<T>> {
    override fun convert(source: String): OkonomiRader<T> = mapper.readValue(source)
}

// object OkonomiRaderMapper {
//
//    fun mapToOkonomiRad(source: String): OkonomiRader<OkonomiRad> {
// //        return mapper.readValue()
//    }
//
// }
