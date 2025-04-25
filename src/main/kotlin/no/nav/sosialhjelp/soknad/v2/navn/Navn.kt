package no.nav.sosialhjelp.soknad.v2.navn

data class Navn(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
)

data class NavnInput(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
)
