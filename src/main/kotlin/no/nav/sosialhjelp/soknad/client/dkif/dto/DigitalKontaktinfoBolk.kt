package no.nav.sosialhjelp.soknad.client.dkif.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class DigitalKontaktinfoBolk(
    val kontaktinfo: Map<String, DigitalKontaktinfo>?,
    val feil: Map<String, Feil>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DigitalKontaktinfo(
    val mobiltelefonnummer: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Feil(
    val melding: String?
)
