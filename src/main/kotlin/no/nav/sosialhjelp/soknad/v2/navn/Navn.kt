package no.nav.sosialhjelp.soknad.v2.navn

import com.fasterxml.jackson.annotation.JsonIgnore

data class Navn(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
) {
    @JsonIgnore
    fun getFulltNavn() = listOfNotNull(fornavn, mellomnavn, etternavn).joinToString(" ")
}

data class NavnInput(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
)
