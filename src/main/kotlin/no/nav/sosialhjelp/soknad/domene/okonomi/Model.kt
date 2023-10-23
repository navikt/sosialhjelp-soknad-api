package no.nav.sosialhjelp.soknad.domene.okonomi

import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent
import org.springframework.data.annotation.Id
import java.time.LocalDate

data class OversiktInntekt (
    @Id val id: Long,
    val type: String,
    val tittel: String,
    val brutto: Int,
    val netto: Int
)
data class OversiktUtgift (
    @Id val id: Long,
    val type: String,
    val tittel: String,
    val belop: Int
)
data class OversiktFormue (
    @Id val id: Long,
    val type: String,
    val tittel: String,
    val belop: Int
)

data class OpplysningUtgift (
    @Id val id: Long,
    val type: String,
    val tittel: String,
    val belop: Int
)

data class OpplysningUtbetaling (
    @Id val id: Long,
    val type: String,
    val tittel: String,
    val orgnummer: String,
    val belop: Int,
    val netto: Int,
    val brutto: Int,
    val skattetrekk: Double,
    val andreTrekk: Double,
    val utbetalingsDato: LocalDate,
    val periodeStart: LocalDate,
    val periodeSlutt: LocalDate,
    val komponent: Set<Komponent>
)
data class Komponent (
    val type: String,
    val belop: Double,
    val satsType: String,
    val satsAntall: Double,
    val satsBelop: Double
)

data class OpplysningBekreftelse (
    @Id val id: Long,
    val type: String,
    val tittel: String,
    val bekreftet: Boolean,
    val bekreftelsesDato: LocalDate
)
data class BostotteSak ( // systemdata
    @Id val id: Long,
    val type: String,
    val dato: LocalDate,
    val status: BostotteStatus,
    val beskrivelse: String,
    val vedtaksstatus: Vedtaksstatus
)