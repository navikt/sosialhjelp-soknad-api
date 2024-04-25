package no.nav.sosialhjelp.soknad.v2.register

import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.arbeid.dto.AnsettelsesperiodeDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsavtaleDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OrganisasjonDto
import no.nav.sosialhjelp.soknad.arbeid.dto.PeriodeDto
import no.nav.sosialhjelp.soknad.arbeid.dto.PersonDto
import no.nav.sosialhjelp.soknad.organisasjon.dto.NavnDto
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer1
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer2

object DefaultValuesForMockedResponses {
    val orgnummer1 = "123456789"
    val orgnummer2 = "987654321"
}

internal fun defaultResponseFromAaregClient(personId: String): List<ArbeidsforholdDto> {
    return listOf(
        ArbeidsforholdDto(
            ansettelsesperiode = AnsettelsesperiodeDto(
                periode = PeriodeDto(
                    fom = LocalDate.of(2000, 1, 1),
                    tom = LocalDate.of(2009, 12, 31))
            ),
            arbeidsavtaler = listOf(
                ArbeidsavtaleDto(stillingsprosent = 100.00),
            ),
            arbeidsforholdId = UUID.randomUUID().toString(),
            arbeidsgiver = OrganisasjonDto(organisasjonsnummer = orgnummer1, type = "fast"),
            arbeidstaker = PersonDto(
                offentligIdent = personId,
                aktoerId =  UUID.randomUUID().toString(),
                type = "ansatt"
            ),
        ),
        ArbeidsforholdDto(
            ansettelsesperiode = AnsettelsesperiodeDto(
                periode = PeriodeDto(
                    fom = LocalDate.of(2010, 1, 1),
                    tom = LocalDate.of(2019, 12, 31))
            ),
            arbeidsavtaler = listOf(
                ArbeidsavtaleDto(stillingsprosent = 100.00),
            ),
            arbeidsforholdId = UUID.randomUUID().toString(),
            arbeidsgiver = OrganisasjonDto(organisasjonsnummer = orgnummer2, type = "fast"),
            arbeidstaker = PersonDto(
                offentligIdent = personId,
                aktoerId =  UUID.randomUUID().toString(),
                type = "ansatt"
            ),
        ),
    )
}

fun defaultResponseFromOrganisasjonClient(orgnummer: String): OrganisasjonNoekkelinfoDto {
    return OrganisasjonNoekkelinfoDto(
        navn = NavnDto(
            navnelinje1 = "Et fantastisk firma:${Math.random()}", null, null, null, null
        ),
        organisasjonsnummer = orgnummer
    )
}