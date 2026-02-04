package no.nav.sosialhjelp.soknad.arbeid.dto

import java.time.LocalDate

data class ArbeidsforholdDto(
    val id: String?,
    val ansettelsesperiode: AnsettelsesperiodeDto,
    val ansettelsesdetaljer: List<AnsettelsesdetaljerDto>?,
    val opplysningspliktig: OpplysningspliktigDto?,
    val arbeidstaker: ArbeidstakerDto?,
    val arbeidssted: ArbeidsstedDto?,
)

data class AnsettelsesperiodeDto(
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
)

data class AnsettelsesdetaljerDto(
    val avtaltStillingsprosent: Double?,
    val ansettelsesform: AnsettelsesformDto?,
    val rapporteringsmaaneder: RapporteringsmaanederDto?,
)

data class AnsettelsesformDto(
    val kode: String?,
    val beskrivelse: String?,
)

data class RapporteringsmaanederDto(
    val fra: String,
    val til: String?,
)

data class OpplysningspliktigDto(
    val type: OpplysningspliktigType,
    val identer: List<IdentInfoDto>,
)

enum class OpplysningspliktigType {
    Hovedenhet,
    Person,
}

data class ArbeidsstedDto(
    val type: ArbeidsstedType,
    val identer: List<IdentInfoDto>,
)

enum class ArbeidsstedType {
    Underenhet,
    Person,
}

data class IdentInfoDto(
    val type: IdentInfoType,
    val ident: String,
    val gjeldende: Boolean?,
)

enum class IdentInfoType {
    AKTORID,
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER,
}

data class ArbeidstakerDto(
    val identer: List<ArbeidstakerIdentDto>,
)

data class ArbeidstakerIdentDto(
    val type: ArbeidstakerIdentType,
    val ident: String,
    val gjeldende: Boolean,
)

enum class ArbeidstakerIdentType {
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER,
    AKTORID,
}
