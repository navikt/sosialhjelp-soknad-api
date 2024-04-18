package no.nav.sosialhjelp.soknad.navenhet.domain

data class NavEnhet(
    val enhetNr: String?,
    val navn: String,
    val kommunenavn: String?,
    val sosialOrgNr: String?,
)
