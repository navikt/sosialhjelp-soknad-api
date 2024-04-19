package no.nav.sosialhjelp.soknad.innsending.dto

data class SendTilUrlFrontend(
    val sendtTil: SoknadMottakerFrontend,
    val id: String,
)

enum class SoknadMottakerFrontend {
    FIKS_DIGISOS_API,
}
