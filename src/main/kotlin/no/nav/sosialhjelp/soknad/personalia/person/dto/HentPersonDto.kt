package no.nav.sosialhjelp.soknad.personalia.person.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class HentPersonDataDto<T>(
    val hentPerson: T?,
)

data class PersonAdressebeskyttelseDto(
    val adressebeskyttelse: List<AdressebeskyttelseDto>?,
)

data class PersonDto(
    val bostedsadresse: List<BostedsadresseDto>?,
    val oppholdsadresse: List<OppholdsadresseDto>?,
    val forelderBarnRelasjon: List<ForelderBarnRelasjonDto>?,
    val navn: List<NavnDto>?,
    val sivilstand: List<SivilstandDto>?,
    val statsborgerskap: List<StatsborgerskapDto>?,
)

data class EktefelleDto(
    val adressebeskyttelse: List<AdressebeskyttelseDto>?,
    val bostedsadresse: List<BostedsadresseDto>?,
    val foedsel: List<FoedselDto>?,
    val navn: List<NavnDto>?,
)

data class BarnDto(
    val adressebeskyttelse: List<AdressebeskyttelseDto>?,
    val bostedsadresse: List<BostedsadresseDto>?,
    val folkeregisterpersonstatus: List<FolkeregisterpersonstatusDto>?,
    val foedsel: List<FoedselDto>?,
    val navn: List<NavnDto>?,
)

data class AdressebeskyttelseDto(
    val gradering: Gradering?,
)

enum class Gradering {
    STRENGT_FORTROLIG_UTLAND, // kode 6 utland
    STRENGT_FORTROLIG, // kode 6
    FORTROLIG, // kode 7
    UGRADERT,
}

data class MetadataDto(
    val master: String,
    val endringer: List<EndringDto>,
)

data class EndringDto(
    val kilde: String,
    val registrert: LocalDateTime,
    val type: String,
)

data class FolkeregisterMetadataDto(
    val ajourholdstidspunkt: LocalDateTime?,
    val kilde: String?,
)

data class ForelderBarnRelasjonDto(
    val relatertPersonsIdent: String?,
    val relatertPersonsRolle: String,
    val minRolleForPerson: String,
)

data class NavnDto(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val metadata: MetadataDto,
    val folkeregistermetadata: FolkeregisterMetadataDto?,
)

data class SivilstandDto(
    val type: SivilstandType,
    val relatertVedSivilstand: String?,
    val metadata: MetadataDto,
    val folkeregistermetadata: FolkeregisterMetadataDto?,
)

enum class SivilstandType {
    UOPPGITT,
    UGIFT,
    GIFT,
    ENKE_ELLER_ENKEMANN,
    SKILT,
    SEPARERT,
    REGISTRERT_PARTNER,
    SEPARERT_PARTNER,
    SKILT_PARTNER,
    GJENLEVENDE_PARTNER,
}

data class StatsborgerskapDto(
    val land: String,
)

data class FoedselDto(
    val foedselsdato: LocalDate,
)

data class FolkeregisterpersonstatusDto(
    val status: String,
)

data class BostedsadresseDto(
    val coAdressenavn: String?,
    val vegadresse: VegadresseDto?,
    val matrikkeladresse: MatrikkeladresseDto?,
    val ukjentBosted: UkjentBostedDto?,
)

data class OppholdsadresseDto(
    val oppholdAnnetSted: String?,
    val coAdressenavn: String?,
    val vegadresse: VegadresseDto?,
    val metadata: MetadataDto?,
    val folkeregistermetadata: FolkeregisterMetadataDto?,
)

data class VegadresseDto(
    val matrikkelId: String?,
    val adressenavn: String?,
    val husnummer: Int?,
    val husbokstav: String?,
    val tilleggsnavn: String?,
    val postnummer: String,
    val kommunenummer: String?,
    val bruksenhetsnummer: String?,
    val bydelsnummer: String?,
)

data class MatrikkeladresseDto(
    val matrikkelId: String?,
    val postnummer: String,
    val tilleggsnavn: String?,
    val kommunenummer: String,
    val bruksenhetsnummer: String?,
)

data class UkjentBostedDto(
    val bostedskommune: String?,
)
