package no.nav.sosialhjelp.soknad.kodeverk.dto

import java.time.LocalDate

data class KodeverkDto(
    val betydninger: Map<String, List<BetydningDto>>?
)

data class BetydningDto(
    val gyldigFra: LocalDate,
    val gyldigTil: LocalDate,
    val beskrivelser: Map<String, BeskrivelseDto>?
)

data class BeskrivelseDto(
    val term: String,
    val tekst: String
)
