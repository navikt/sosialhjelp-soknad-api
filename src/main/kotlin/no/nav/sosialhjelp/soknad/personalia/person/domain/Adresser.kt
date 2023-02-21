package no.nav.sosialhjelp.soknad.personalia.person.domain

data class Bostedsadresse(
    val coAdressenavn: String?,
    val vegadresse: Vegadresse?,
    val matrikkeladresse: Matrikkeladresse?
)

/**
 * Fra PDL-doc: https://pdldocs-navno.msappproxy.net/ekstern/index.html#opplysningstyper-adresser-oppholdsAdresse
 * Matrikkeladresse benyttes ytterst sjelden, og aldri når PDL er master. Ingen gyldig oppholdsadresser med matrikkeladresse funnet pr 26.juni.2020
 */
data class Oppholdsadresse(
    val coAdressenavn: String?,
    val vegadresse: Vegadresse?
)

data class Vegadresse(
    val adressenavn: String?,
    val husnummer: Int?,
    val husbokstav: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val poststed: String?,
    val kommunenummer: String?,
    val bruksenhetsnummer: String?,
    val bydelsnummer: String?
)

/**
 * I JsonMatrikkeladresse kan vi sette "kommunenummer", "gaardsnummer", "bruksnummer", "festenummer", "seksjonsnummer", "undernummer"
 */
data class Matrikkeladresse(
    val matrikkelId: String?,
    val postnummer: String?,
    val poststed: String?,
    val tilleggsnavn: String?,
    val kommunenummer: String?,
    val bruksenhetsnummer: String?
)
