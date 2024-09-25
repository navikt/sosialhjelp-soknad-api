package no.nav.sosialhjelp.soknad.innsending.dto

import java.time.LocalDate

data class SendTilUrlFrontend(
    val sendtTil: SoknadMottakerFrontend,
    val id: String,
    val forrigeSoknadSendt: LocalDate?,
    val antallDokumenter: Int,
    val prosentFyltUt: Double,
    val kortSoknad: Boolean,
)

enum class SoknadMottakerFrontend {
    FIKS_DIGISOS_API,
}
