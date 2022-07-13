package no.nav.sosialhjelp.soknad.arbeid.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ArbeidsforholdDto(
    val ansettelsesperiode: AnsettelsesperiodeDto?,
    val arbeidsavtaler: List<ArbeidsavtaleDto>?,
    val arbeidsforholdId: String?,
    val arbeidsgiver: OpplysningspliktigArbeidsgiverDto?,
    val arbeidstaker: PersonDto?
)

data class AnsettelsesperiodeDto(
    val periode: PeriodeDto
)

data class ArbeidsavtaleDto(
    val stillingsprosent: Double
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = OrganisasjonDto::class, name = "Organisasjon"),
    JsonSubTypes.Type(value = PersonDto::class, name = "Person"),
)
sealed class OpplysningspliktigArbeidsgiverDto()

data class OrganisasjonDto(
    val organisasjonsnummer: String?,
    val type: String?
) : OpplysningspliktigArbeidsgiverDto()

data class PersonDto(
    val offentligIdent: String,
    val aktoerId: String,
    val type: String?
) : OpplysningspliktigArbeidsgiverDto()

data class PeriodeDto(
    val fom: LocalDate,
    val tom: LocalDate?
)

fun ArbeidsforholdDto.toDomain(organisasjonService: OrganisasjonService): Arbeidsforhold {
    return Arbeidsforhold(
        orgnr = if (this.arbeidsgiver is OrganisasjonDto) this.arbeidsgiver.organisasjonsnummer else null,
        arbeidsgivernavn = if (this.arbeidsgiver is OrganisasjonDto) organisasjonService.hentOrgNavn(this.arbeidsgiver.organisasjonsnummer) else "Privatperson",
        fom = this.ansettelsesperiode?.periode?.fom?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        tom = this.ansettelsesperiode?.periode?.tom?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        fastStillingsprosent = this.arbeidsavtaler?.sumOf { it.stillingsprosent }?.toLong(),
        harFastStilling = this.arbeidsavtaler?.isNotEmpty()
    )
}
