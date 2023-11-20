package no.nav.sosialhjelp.soknad.nymodell.controller.dto

import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg

data class AdresseResponse(
    val adresseValg: AdresseValg? = null,
    val folkeregistrertAdresse: AdresseObjectDto? = null,
    val oppholdsadresse: AdresseObjectDto? = null,
    val midlertidigAdresse: AdresseObjectDto? = null
)

data class AdresseRequest(
    val valg: AdresseValg,
    val adresseSoknad: AdresseObjectDto
)

interface AdresseObjectDto

data class GateadresseDto(
    val landkode: String = "NO",
    val kommunenummer: String? = null,
    val adresselinjer: List<String> = emptyList(),
    val bolignummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val gatenavn: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null,
) : AdresseObjectDto

data class MatrikkeladresseDto(
    val kommunenummer: String? = null,
    val gaardsnummer: String? = null,
    val bruksnummer: String? = null,
    val festenummer: String? = null,
    val seksjonsnummer: String? = null,
    val undernummer: String? = null,
) : AdresseObjectDto

data class UstrukturertAdresseDto(
    val adresse: List<String> = emptyList()
) : AdresseObjectDto
