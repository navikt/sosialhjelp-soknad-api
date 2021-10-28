package no.nav.sosialhjelp.soknad.client.husbanken.domain

import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteMottaker
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteRolle
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteStatus
import java.math.BigDecimal
import java.time.LocalDate

data class Bostotte(
    val saker: List<Sak>,
    val utbetalinger: List<Utbetaling>
)

data class Sak(
    val mnd: Int,
    val ar: Int,
    val status: BostotteStatus,
    val vedtak: Vedtak?,
    val rolle: BostotteRolle
) {
    val dato: LocalDate
        get() = LocalDate.of(ar, mnd, 1)
}

data class Vedtak(
    val kode: String,
    val beskrivelse: String,
    val type: String?
)

data class Utbetaling(
    val utbetalingsdato: LocalDate?,
    val belop: BigDecimal,
    val mottaker: BostotteMottaker,
    val rolle: BostotteRolle
)
