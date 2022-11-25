package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain

data class KartverketMatrikkelAdresse(
    val kommunenummer: String?,
    val gaardsnummer: String?,
    val bruksnummer: String?,
    val festenummer: String?,
    val seksjonsunmmer: String?,
    val undernummer: String?
)
