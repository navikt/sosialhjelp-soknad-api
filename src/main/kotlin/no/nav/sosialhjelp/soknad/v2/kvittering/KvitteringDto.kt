package no.nav.sosialhjelp.soknad.v2.kvittering

import java.time.LocalDateTime

data class KvitteringDto(
    val digisosId: String,
    val tidspunktSendt: LocalDateTime,
    val filnavn: String,
    val storrelse: Long,
    val soknadPdfUrl: String,
)
