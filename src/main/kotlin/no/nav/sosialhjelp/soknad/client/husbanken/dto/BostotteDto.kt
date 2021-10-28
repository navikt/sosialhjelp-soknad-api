package no.nav.sosialhjelp.soknad.client.husbanken.dto

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import java.math.BigDecimal
import java.time.LocalDate

data class BostotteDto(
    val saker: List<SakDto>,
    val utbetalinger: List<UtbetalingDto>
)

data class SakDto(
    val mnd: Int,
    val ar: Int,
    val status: BostotteStatus,
    val vedtak: VedtakDto,
    val rolle: BostotteRolle
)

enum class BostotteStatus {
    UNDER_BEHANDLING, VEDTATT
}

enum class BostotteRolle {
    HOVEDPERSON, BIPERSON
}

data class VedtakDto(
    val kode: String,
    val beskrivelse: String,
    val type: String
)

data class UtbetalingDto(
    val utbetalingsdato: LocalDate,
    val belop: BigDecimal,
    val mottaker: BostotteMottaker,
    val rolle: BostotteRolle
)

enum class BostotteMottaker(
    private val value: String
) {
    KOMMUNE(JsonOkonomiOpplysningUtbetaling.Mottaker.KOMMUNE.value()),
    HUSSTAND(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND.value())
}
