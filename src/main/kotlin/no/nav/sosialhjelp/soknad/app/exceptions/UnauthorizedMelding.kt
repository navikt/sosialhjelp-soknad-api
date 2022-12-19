package no.nav.sosialhjelp.soknad.app.exceptions

import java.net.URI

data class UnauthorizedMelding(
    val id: String?,
    val message: String?,
    val loginUrl: String?
) {
    constructor(id: String?, message: String?, loginUrl: URI?) : this(id, message, loginUrl?.toString())
}
