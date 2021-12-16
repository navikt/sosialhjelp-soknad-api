package no.nav.sosialhjelp.soknad.api.informasjon.dto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PabegyntSoknad(
    private val sistOppdatert: LocalDateTime,
    val behandlingsId: String
) {

    fun getSistOppdatert(): String {
        return sistOppdatert.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}
