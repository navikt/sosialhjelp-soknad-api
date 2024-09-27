package no.nav.sosialhjelp.soknad.innsending.dto

import java.time.LocalDateTime

data class SendTilUrlFrontend(
    val sendtTil: SoknadMottakerFrontend,
    val id: String,
    val forrigeSoknadSendt: LocalDateTime?,
    val antallDokumenter: Int,
)

enum class SoknadMottakerFrontend {
    FIKS_DIGISOS_API,
}
