package no.nav.sosialhjelp.soknad.api.nedetid.dto

data class NedetidFrontend(
    val isNedetid: Boolean = false,
    val isPlanlagtNedetid: Boolean = false,
    val nedetidStart: String? = null,
    val nedetidSlutt: String? = null,
    val nedetidStartText: String? = null,
    val nedetidSluttText: String? = null,
    val nedetidStartTextEn: String? = null,
    val nedetidSluttTextEn: String? = null,
)
