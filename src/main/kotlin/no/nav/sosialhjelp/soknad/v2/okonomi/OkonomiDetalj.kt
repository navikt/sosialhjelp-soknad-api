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

// Wrapper-objekt som persisteres som en json-streng i databasen
data class OkonomiDetaljer<T : OkonomiDetalj>(
    val detaljer: List<T> = emptyList(),
)

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
    JsonSubTypes.Type(value = AvdragRenter::class, name = "AvdragRenter"),
    JsonSubTypes.Type(value = Utbetaling::class, name = "Utbetaling"),
    JsonSubTypes.Type(value = UtbetalingMedKomponent::class, name = "UtbetalingMedKomponent"),
)
sealed interface OkonomiDetalj

// For UTGIFTER_ANNET_BARN, UTGIFTER_ANNET_BO og "UTGIFTER_ANDRE_UTGIFTER" knyttes beskrivelse til det eksakte beløpet
data class Belop(
    val belop: Double,
    val beskrivelse: String? = null,
) : OkonomiDetalj

data class BruttoNetto(
    val brutto: Double? = null,
    val netto: Double? = null,
) : OkonomiDetalj

// Spesialhåndtering fordi avdrag og renter knyttes sammen
data class AvdragRenter(
    val avdrag: Double? = null,
    val renter: Double? = null,
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
    val organisasjon: Organisasjon? = null,
) : OkonomiDetalj

data class Organisasjon(
    val navn: String? = null,
    val orgnummer: String? = null,
)

data class UtbetalingMedKomponent(
    val utbetaling: Utbetaling = Utbetaling(),
    val komponenter: List<Komponent> = emptyList(),
) : OkonomiDetalj

data class Komponent(
    val type: String? = null,
    val belop: Double? = null,
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
class OkonomiskeDetaljerToStringConverter<T : OkonomiDetalj> : Converter<OkonomiDetaljer<T>, String> {
    override fun convert(source: OkonomiDetaljer<T>): String = mapper.writeValueAsString(source)
}

@ReadingConverter
class StringToOkonomiskeDetaljerConverter<T : OkonomiDetalj> : Converter<String, OkonomiDetaljer<T>> {
    override fun convert(source: String): OkonomiDetaljer<T> = mapper.readValue(source)
}
