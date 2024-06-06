package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.annotation.Id
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OkonomiRepository : UpsertRepository<Okonomi>, ListCrudRepository<Okonomi, UUID>

@Table
data class Okonomi(
    @Id val soknadId: UUID,
    val inntekter: Set<Inntekt> = emptySet(),
    val utgifter: Set<Utgift> = emptySet(),
    val formuer: Set<Formue> = emptySet(),
    val bekreftelser: Set<Bekreftelse> = emptySet(),
) : DomainRoot {
    override fun getDbId() = soknadId
}

@Table
data class Bekreftelse(
    val type: BekreftelseType,
    val verdi: Boolean,
)

enum class BekreftelseType {
    // TODO Vedrørende fjerning av disse - de introduserer egentlig et 3.nivå av "svar"..
    // TODO hvis bruker IKKE svarer, vil det ikke finnes bekreftelse
    // TODO hvis bruker svarer nei, vil denne være false
    // TODO hvis bruker svarer ja, vil den være true
    // TODO Hvordan utlede dette kun på bakgrunn av om det finnes en f.eks. Formue eller ikke ?
    BEKREFTELSE_BARNEUTGIFTER,
    BEKREFTELSE_BOUTGIFTER,
    BEKREFTELSE_SPARING,
    BEKREFTELSE_UTBETALING,
    BEKREFTELSE_VERDI,

    // // TODO Samme som over
    BOSTOTTE,

    // TODO Samtykker kan leve i en 2-dimensjonal greie -> Gitt / Ikke gitt. Kunne kanskje vært persistert et annet sted?
    BOSTOTTE_SAMTYKKE,
    STUDIELAN_BEKREFTELSE,
    UTBETALING_SKATTEETATEN_SAMTYKKE,
}

// Inntekt, Utgift, Formue
interface OkonomiElement {
    val type: OkonomiType
    val beskrivelse: String?
}

// InntektType, UtgiftType, FormueType
interface OkonomiType {
    // denne må hete `name` for pga enum.name
    val name: String
    val dokumentasjonForventet: Boolean
}

@WritingConverter
object OkonomiTypeToStringConverter : Converter<OkonomiType, String> {
    override fun convert(source: OkonomiType): String = source.name
}

@ReadingConverter
object StringToOkonomiTypeConverter : Converter<String, OkonomiType> {
    override fun convert(source: String): OkonomiType = StringToOkonomiTypeMapper.map(source)
}

private object StringToOkonomiTypeMapper {
    fun map(typeString: String): OkonomiType {
        return InntektType.entries.find { it.name == typeString }
            ?: UtgiftType.entries.find { it.name == typeString }
            ?: FormueType.entries.find { it.name == typeString }
            ?: error("Kunne ikke mappe OkonomiType")
    }
}
