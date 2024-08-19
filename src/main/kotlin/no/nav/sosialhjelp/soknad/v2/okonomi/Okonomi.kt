package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface OkonomiRepository : UpsertRepository<Okonomi>, ListCrudRepository<Okonomi, UUID>

@Table
data class Okonomi(
    @Id val soknadId: UUID,
    // TODO inntekter, utgifter, formuer og bekreftelser bør være map for å gjenspeile kun 1 innslag pr. type
    // TODO eventuelt for set må equals-metoden for disse kun sammenlikne på typen
    val inntekter: Set<Inntekt> = emptySet(),
    val utgifter: Set<Utgift> = emptySet(),
    val formuer: Set<Formue> = emptySet(),
    val bekreftelser: Set<Bekreftelse> = emptySet(),
    val bostotteSaker: List<BostotteSak> = emptyList(),
) : DomainRoot {
    override fun getDbId() = soknadId
}

@Table
data class Bekreftelse(
    val type: BekreftelseType,
    val dato: LocalDate = LocalDate.now(),
    val verdi: Boolean,
)

@Table
data class BostotteSak(
    val dato: LocalDate,
    val status: BostotteStatus,
    val beskrivelse: String?,
    val vedtaksstatus: Vedtaksstatus?,
)

enum class BekreftelseType {
    BEKREFTELSE_BARNEUTGIFTER,
    BEKREFTELSE_BOUTGIFTER,
    BEKREFTELSE_SPARING,
    BEKREFTELSE_UTBETALING,
    BEKREFTELSE_VERDI,
    STUDIELAN_BEKREFTELSE,

    BOSTOTTE,

    BOSTOTTE_SAMTYKKE,
    UTBETALING_SKATTEETATEN_SAMTYKKE,
}

enum class Vedtaksstatus {
    INNVILGET,
    AVSLAG,
    AVVIST,
}

enum class BostotteStatus {
    UNDER_BEHANDLING,
    VEDTATT,
}

// Inntekt, Utgift, Formue
interface OkonomiElement {
    val type: OkonomiType
    val beskrivelse: String?
}
