package no.nav.sosialhjelp.soknad.client.dkif.dto

data class DigitalKontaktinfoBolk(
    val kontaktinfo: Map<String, DigitalKontaktinfo>?,
    val feil: Map<String, Feil>?
)

data class DigitalKontaktinfo(
    val mobiltelefonnummer: String?
)

data class Feil(
    val melding: String?
)
