package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto

data class HentAdresseDataDto(
    val hentAdresse: AdresseDto?
)

data class AdresseDto(
    val matrikkeladresse: MatrikkeladresseDto?
)

data class MatrikkeladresseDto(
    val undernummer: String?,
    val matrikkelnummer: MatrikkelNummer?,
    val bydel: Bydel?
)

data class MatrikkelNummer(
    val kommunenummer: String?,
    val gaardsnummer: String?,
    val bruksnummer: String?,
    val festenummer: String?,
    val seksjonsnummer: String?
)

data class Bydel(
    val bydelsnummer: String?
)
