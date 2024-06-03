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
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OkonomiRepository : UpsertRepository<Okonomi>, ListCrudRepository<Okonomi, UUID>

@Table
data class Okonomi(
    @Id val soknadId: UUID,
    val inntekter: List<Inntekt> = emptyList(),
    val utgifter: List<Utgift> = emptyList(),
    val formuer: List<Formue> = emptyList(),
    val bekreftelser: Set<Bekreftelse> = emptySet(),
    @Embedded.Empty
    val beskrivelserAnnet: BeskrivelserAnnet = BeskrivelserAnnet(),
) : DomainRoot {
    override fun getDbId() = soknadId
}

@Table
data class Bekreftelse(
    val type: BekreftelseType,
    val verdi: Boolean,
)

@Table
data class BeskrivelserAnnet(
    @Column("beskrivelse_verdi")
    val verdi: String? = null,
    @Column("beskrivelse_sparing")
    val sparing: String? = null,
    @Column("beskrivelse_utbetaling")
    val utbetaling: String? = null,
    @Column("beskrivelse_boutgifter")
    val boutgifter: String? = null,
    @Column("beskrivelse_barneutgifter")
    val barneutgifter: String? = null,
)

enum class BekreftelseType(val tittelKey: String) {
    BEKREFTELSE_BARNEUTGIFTER(tittelKey = "utgifter.barn"),
    BEKREFTELSE_BOUTGIFTER(tittelKey = "utgifter.boutgift"),
    BEKREFTELSE_SPARING(tittelKey = "inntekt.bankinnskudd"),
    BEKREFTELSE_UTBETALING(tittelKey = "inntekt.inntekter"),
    BEKREFTELSE_VERDI(tittelKey = "inntekt.eierandeler"),

    // TODO sjekk bruk av disse
    BOSTOTTE(tittelKey = "inntekt.bostotte"),

    // TODO sjekk bruk av disse
    BOSTOTTE_SAMTYKKE(tittelKey = "inntekt.bostotte.samtykke"),
    STUDIELAN_BEKREFTELSE(tittelKey = "inntekt.student"),
    UTBETALING_SKATTEETATEN_SAMTYKKE(tittelKey = "utbetalinger.skattbar.samtykke"),
}

interface OkonomiType {
    // denne må hete `name` for å override enum.name
    val name: String
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
    private val okonomiTyper = mutableSetOf<OkonomiType>()

    init {
        okonomiTyper.addAll(FormueType.entries)
        okonomiTyper.addAll(InntektType.entries)
        okonomiTyper.addAll(UtgiftType.entries)
    }

    fun map(typeString: String): OkonomiType {
        return okonomiTyper.firstOrNull { it.name == typeString } ?: error("Fant ikke OkonomiType")
    }
}
