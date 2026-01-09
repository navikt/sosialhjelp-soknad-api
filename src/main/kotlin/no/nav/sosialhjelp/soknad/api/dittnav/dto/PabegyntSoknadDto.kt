package no.nav.sosialhjelp.soknad.api.dittnav.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/**
 * Response-objekt for endepunkt som skal hente informasjon om påbegynte søknader for Min Side.
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
    @get:JsonProperty("isAktiv")
    val isAktiv: Boolean,
    val soknadId: UUID,
)
