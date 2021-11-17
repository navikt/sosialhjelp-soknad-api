package no.nav.sosialhjelp.soknad.adressesok.dto

data class AdressesokDataDto(
    val sokAdresse: AdressesokResultDto
)

data class AdressesokResultDto(
    val hits: List<AdressesokHitDto>?,
    val pageNumber: Int,
    val totalPages: Int,
    val totalHits: Int
)

data class AdressesokHitDto(
    val vegadresse: VegadresseDto,
    val score: Float
)

data class VegadresseDto(
    val matrikkelId: String,
    val husnummer: Int?,
    val husbokstav: String?,
    val adressenavn: String?,
    val kommunenavn: String?,
    val kommunenummer: String?,
    val postnummer: String?,
    val poststed: String?,
    val bydelsnummer: String?
)
