package no.nav.sosialhjelp.soknad.v2.navn

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
) {
    fun getFulltNavn() = "$fornavn ${ mellomnavn?.let { "$it " }} $etternavn"
}
