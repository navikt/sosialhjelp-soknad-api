package no.nav.sosialhjelp.soknad.navenhet.dto

data class NavEnhetFrontend(
    val orgnr: String?,
    val enhetsnr: String?,
    val enhetsnavn: String,
    val kommunenavn: String?,
    val kommuneNr: String? = null,
    val behandlingsansvarlig: String? = null,
    val valgt: Boolean? = null,
    val isMottakMidlertidigDeaktivert: Boolean? = null,
    val isMottakDeaktivert: Boolean? = null
)
