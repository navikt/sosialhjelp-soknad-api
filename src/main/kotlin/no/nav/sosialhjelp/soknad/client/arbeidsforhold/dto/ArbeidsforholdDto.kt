package no.nav.sosialhjelp.soknad.client.arbeidsforhold.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

data class ArbeidsforholdDto(
    val ansettelsesperiode: AnsettelsesperiodeDto,
    val arbeidsavtaler: List<ArbeidsavtaleDto>,
    val arbeidsforholdId: String,
    val arbeidsgiver: OpplysningspliktigArbeidsgiverDto,
    val arbeidstaker: PersonDto,
    val navArbeidsforholdId: Long
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
    val tom: LocalDate
)
