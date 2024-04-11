package no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto

data class DigitalKontaktinformasjon(
    val personident: String,
    val aktiv: Boolean,
    val kanVarsles: Boolean?,
    val reservert: Boolean?,
    val mobiltelefonnummer: String?,
)
