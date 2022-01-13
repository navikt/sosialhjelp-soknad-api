package no.nav.sosialhjelp.soknad.common.rest.feil

import java.net.URI

data class UnauthorizedMelding(
    val id: String?,
    val message: String?,
    val loginUrl: String?
) {
    constructor(id: String?, message: String?, loginUrl: URI?) : this(id, message, loginUrl?.toString())
}
