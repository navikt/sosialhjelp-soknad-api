package no.nav.sosialhjelp.soknad.app.exceptions

data class Feilmelding(
    val id: String?,
    val message: String?
) {
    companion object {
        const val NO_BIGIP_5XX_REDIRECT = "X-Escape-5xx-Redirect"
    }
}
