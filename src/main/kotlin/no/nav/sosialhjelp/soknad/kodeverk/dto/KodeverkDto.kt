package no.nav.sosialhjelp.soknad.kodeverk.dto

import java.io.Serializable
import java.time.LocalDate

data class KodeverkDto(
    val betydninger: Map<String, List<BetydningDto>>,
) : Serializable

data class BetydningDto(
    val gyldigFra: LocalDate,
    val gyldigTil: LocalDate,
    val beskrivelser: Map<String, BeskrivelseDto>,
) : Serializable

data class BeskrivelseDto(
    val term: String,
    val tekst: String,
) : Serializable
