package no.nav.sosialhjelp.soknad.innsending.dto

data class StartSoknadResponse(
    val brukerBehandlingId: String,
    val useKortSoknad: Boolean,
)
