package no.nav.sosialhjelp.soknad.api.innsyn.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class SoknadOversiktDto(
    val fiksDigisosId: String? = null,
    val soknadTittel: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val sistOppdatert: Date? = null,
    val kilde: String? = null,
    val url: String? = null
)
