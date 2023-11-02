package no.nav.sosialhjelp.soknad.domene.okonomi

import no.nav.sosialhjelp.soknad.domene.Kilde
import no.nav.sosialhjelp.soknad.domene.okonomi.type.BekreftelseType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.OkonomiType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.UtgiftType
import no.nav.sosialhjelp.soknad.domene.soknad.SoknadBubbleObject
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.LocalDate
import java.util.*

interface OkonomiBubbleObject: SoknadBubbleObject {
    val type: OkonomiType
}

data class Utgift (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    override val type: UtgiftType,
    val tittel: String? = null,
    val belop: Int? = null,
): OkonomiBubbleObject

data class Formue (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    override val type: FormueType,
    val tittel: String? = null,
    val belop: Int? = null
): OkonomiBubbleObject

data class Inntekt (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    override val type: InntektType,
    val tittel: String? = null,
    val brutto: Int? = null,
    val netto: Int? = null,
    val utbetaling: Utbetaling? = null,
): OkonomiBubbleObject

data class Utbetaling (
    val kilde: Kilde,
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
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: BekreftelseType? = null,
    val tittel: String? = null,
    val bekreftet: Boolean? = null,
    val bekreftelsesDato: LocalDate = LocalDate.now()
): SoknadBubbleObject

data class Bostotte ( // systemdata
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: String? = null,
    val dato: LocalDate? = null,
    val status: BostotteStatus? = null,
    val beskrivelse: String? = null,
    val vedtaksstatus: Vedtaksstatus? = null,
): SoknadBubbleObject

data class BeskrivelserAvAnnet (
    @Id val soknadId: UUID,
    val verdi: String? = null,
    val sparing: String? = null,
    val utbetaling: String? = null,
    val boutgifter: String? = null,
    val barneutgifter: String? = null
): SoknadBubbleObject { override val id: UUID get() = soknadId }