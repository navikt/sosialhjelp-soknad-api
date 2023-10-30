package no.nav.sosialhjelp.soknad.domene.okonomi

import no.nav.sosialhjelp.soknad.domene.soknad.SoknadBubbleObject
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.LocalDate
import java.util.*

data class Utgift (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OkonomiType? = null,
    val tittel: String? = null,
    val belop: Int? = null,
    @MappedCollection(idColumn = "REF_ID")
    val bekreftelse: Bekreftelse? = null
): SoknadBubbleObject
data class Formue (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OkonomiType? = null,
    val tittel: String? = null,
    val belop: Int? = null
): SoknadBubbleObject
data class Inntekt (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OkonomiType? = null,
    val tittel: String? = null,
    val brutto: Int? = null,
    val netto: Int? = null,
    val utbetaling: Utbetaling? = null,
    @MappedCollection(idColumn = "REF_ID")
    val bekreftelse: Bekreftelse? = null
): SoknadBubbleObject

data class Utbetaling (
    val orgnummer: String? = null,
    val belop: Int? = null,
    val skattetrekk: Double? = null,
    val andreTrekk: Double? = null,
    val utbetalingsdato: LocalDate? = null,
    val periodeStart: LocalDate? = null,
    val periodeSlutt: LocalDate? = null,
    @MappedCollection(idColumn = "UTBETALING")
    val komponent: Set<Komponent>? = null
)

data class Komponent (
    val type: String? = null,
    val belop: Double? = null,
    val satsType: String? = null,
    val satsAntall: Double? = null,
    val satsBelop: Double? = null
)

data class Bekreftelse (
//    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: String? = null,
    val tittel: String? = null,
    val bekreftet: Boolean? = null,
    val bekreftelsesDato: LocalDate = LocalDate.now()
)
//    : SoknadBubbleObject

data class Bostotte ( // systemdata
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: String? = null,
    val dato: LocalDate? = null,
    val status: BostotteStatus? = null,
    val beskrivelse: String? = null,
    val vedtaksstatus: Vedtaksstatus? = null,
): SoknadBubbleObject