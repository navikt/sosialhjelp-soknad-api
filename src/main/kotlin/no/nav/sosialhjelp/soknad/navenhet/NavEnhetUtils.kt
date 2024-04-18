package no.nav.sosialhjelp.soknad.navenhet

object NavEnhetUtils {
    private const val SPLITTER = ", "

    fun createNavEnhetsnavn(
        enhetsnavn: String,
        kommunenavn: String?,
    ): String {
        return enhetsnavn + SPLITTER + kommunenavn
    }

    fun getEnhetsnavnFromNavEnhetsnavn(navEnhetsnavn: String): String {
        return navEnhetsnavn.split(SPLITTER)[0]
    }

    fun getKommunenavnFromNavEnhetsnavn(navEnhetsnavn: String): String {
        return navEnhetsnavn.split(SPLITTER)[1]
    }
}
