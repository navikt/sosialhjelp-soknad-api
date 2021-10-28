package no.nav.sosialhjelp.soknad.client.husbanken.dto

import no.nav.sosialhjelp.soknad.client.husbanken.domain.Bostotte
import no.nav.sosialhjelp.soknad.client.husbanken.domain.Sak
import no.nav.sosialhjelp.soknad.client.husbanken.domain.Utbetaling
import no.nav.sosialhjelp.soknad.client.husbanken.domain.Vedtak
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteMottaker
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteRolle
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteStatus
import java.math.BigDecimal
import java.time.LocalDate

data class BostotteDto(
    val saker: List<SakDto>?,
    val utbetalinger: List<UtbetalingDto>?
) {
    fun toDomain(): Bostotte {
        return Bostotte(
            saker
                ?.filter { it.rolle == BostotteRolle.HOVEDPERSON }
                ?.map { it.toDomain } ?: emptyList(),
            utbetalinger
                ?.filter { it.rolle == BostotteRolle.HOVEDPERSON }
                ?.map { it.toDomain } ?: emptyList()
        )
    }
}

data class SakDto(
    val mnd: Int,
    val ar: Int,
    val status: BostotteStatus,
    val vedtak: VedtakDto?,
    val rolle: BostotteRolle
)

data class VedtakDto(
    val kode: String,
    val beskrivelse: String,
    val type: String?
)

data class UtbetalingDto(
    val utbetalingsdato: LocalDate?,
    val belop: BigDecimal,
    val mottaker: BostotteMottaker,
    val rolle: BostotteRolle
)

val SakDto.toDomain: Sak
    get() = Sak(mnd, ar, status, vedtak?.toDomain, rolle)

val VedtakDto.toDomain: Vedtak
    get() = Vedtak(kode, beskrivelse, type)

val UtbetalingDto.toDomain: Utbetaling
    get() = Utbetaling(utbetalingsdato, belop, mottaker, rolle)
