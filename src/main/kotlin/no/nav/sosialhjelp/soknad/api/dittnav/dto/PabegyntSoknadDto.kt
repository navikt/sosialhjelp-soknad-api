package no.nav.sosialhjelp.soknad.api.dittnav.dto

/**
 * Response-objekt for endepunkt som skal hente informasjon om påbegynte søknader for DittNAV.
 * https://navikt.github.io/brukernotifikasjon-docs/eventtyper/beskjed/felter/
 */
class PabegyntSoknadDto(
    val eventTidspunkt: String,
    val eventId: String,
    val grupperingsId: String,
    val tekst: String,
    val link: String,
    val sikkerhetsnivaa: Int,
    val sistOppdatert: String,
    val isAktiv: Boolean
)

data class MarkerPabegyntSoknadSomLestDto(
    val eventId: String,
    val grupperingsId: String
)
