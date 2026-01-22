package no.nav.sosialhjelp.soknad.arbeid.dto

import java.time.LocalDate

data class ArbeidsforholdDtoV2(
    val id: String,
    val ansettelsesperiode: AnsettelsesperiodeDtoV2,
    val ansettelsesdetaljer: List<AnsettelsesdetaljerDtoV2>?,
    val opplysningspliktig: OpplysningspliktigDto?,
    val arbeidstaker: ArbeidstakerDto?,
)

data class AnsettelsesperiodeDtoV2(
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
)

data class AnsettelsesdetaljerDtoV2(
    val avtaltStillingsprosent: Double,
    val ansettelsesform: AnsettelsesformDto?,
)

data class AnsettelsesformDto(
    val kode: String?,
    val beskrivelse: String?,
)

data class OpplysningspliktigDto(
    val type: OpplysningspliktigType,
    val identer: List<OpplysningspliktigIdentDto>,
)

enum class OpplysningspliktigType {
    Hovedenhet,
    Person,
}

data class OpplysningspliktigIdentDto(
    val type: OpplysningspliktigIdentType,
    val ident: String,
    val gjeldende: Boolean?,
)

enum class OpplysningspliktigIdentType {
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
