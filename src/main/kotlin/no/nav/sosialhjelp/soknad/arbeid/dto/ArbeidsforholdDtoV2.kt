package no.nav.sosialhjelp.soknad.arbeid.dto

data class ArbeidsforholdDtoV2(
    val id: String,
    val ansettelsesperiode: AnsettelsesperiodeDtoV2?,
    val ansettelsesdetaljer: List<AnsettelsesdetaljerDtoV2>?,
    val opplysningspliktig: OpplysningspliktigDto?,
    val arbeidstaker: ArbeidstakerDto?,
)

data class AnsettelsesperiodeDtoV2(
    val startdato: String,
    val sluttdato: String?,
)

data class AnsettelsesdetaljerDtoV2(
    val avtaltStillingsprosent: Double,
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
    val gjeldense: Boolean,
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
