
package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.OkonomiType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.UtgiftType
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.UuidAsIdObject
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.LocalDate
import java.util.*

interface OneOfManyObject: UuidAsIdObject
interface OkonomiObject: OneOfManyObject {
    val type: OkonomiType
}

data class Utgift (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    override val type: UtgiftType,
    val tittel: String? = null,
    val belop: Int? = null,
): OkonomiObject

data class Formue (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    override val type: FormueType,
    val tittel: String? = null,
    val belop: Int? = null
): OkonomiObject

data class Inntekt (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    override val type: InntektType,
    val tittel: String? = null,
    val brutto: Int? = null,
    val netto: Int? = null,
    val utbetaling: Utbetaling? = null,
): OkonomiObject

data class Utbetaling (
    val kilde: Kilde,
    val orgnummer: String? = null,
    val belop: Int? = null,
    val skattetrekk: Double? = null,
    val andreTrekk: Double? = null,
    val utbetalingsdato: LocalDate? = null,
    val periodeStart: LocalDate? = null,
    val periodeSlutt: LocalDate? = null,
    val komponent: Set<Komponent>? = null
)

data class Komponent (
    val type: String? = null,
    val belop: Double? = null,
    val satsType: String? = null,
    val satsAntall: Double? = null,
    val satsBelop: Double? = null
)