package no.nav.sosialhjelp.soknad.arbeid

import io.getunleash.Unleash
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.arbeid.dto.AnsettelsesperiodeDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsavtaleDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OrganisasjonDto
import no.nav.sosialhjelp.soknad.arbeid.dto.PeriodeDto
import no.nav.sosialhjelp.soknad.arbeid.dto.PersonDto
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class ArbeidsforholdServiceTest {

    private val aaregClient: AaregClient = mockk()
    private val organisasjonService: OrganisasjonService = mockk()
    private val unleash: Unleash = mockk()
    private val arbeidsforholdService = ArbeidsforholdService(aaregClient, organisasjonService)

    private val fnr = "11111111111"
    private val orgnr = "orgnr"
    private val orgNavn = "Testbedriften A/S"
    private val fom = LocalDate.now().minusMonths(1)
    private val tom = LocalDate.now()

    private val arbeidsgiverOrganisasjon = OrganisasjonDto(organisasjonsnummer = orgnr, type = "Organisasjon")
    private val arbeidsgiverPerson = PersonDto(
        offentligIdent = "arbeidsgiver_fnr",
        aktoerId = "aktoerid",
        type = "Person"
    )
    private val arbeidstaker = PersonDto(offentligIdent = "arbeidstaker_fnr", aktoerId = "aktoerid", type = "Person")

    @BeforeEach
    fun setUp() {
        every { unleash.isEnabled(any(), false) } returns false
    }

    @Test
    internal fun skalMappeDtoTilArbeidsforhold() {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(fnr) } returns listOf(createArbeidsforhold(true, fom, tom))
        every { organisasjonService.hentOrgNavn(any()) } returns orgNavn

        val arbeidsforholdList: List<Arbeidsforhold>? = arbeidsforholdService.hentArbeidsforhold(fnr)

        assertThat(arbeidsforholdList).isNotNull
        val arbeidsforhold = arbeidsforholdList!![0]
        assertThat(arbeidsforhold.arbeidsgivernavn).isEqualTo(orgNavn)
        assertThat(arbeidsforhold.harFastStilling).isTrue
        assertThat(arbeidsforhold.fastStillingsprosent).isEqualTo(100L)
        assertThat(arbeidsforhold.orgnr).isEqualTo(orgnr)
        assertThat(arbeidsforhold.fom).isEqualTo(fom.format(DateTimeFormatter.ISO_LOCAL_DATE))
        assertThat(arbeidsforhold.tom).isEqualTo(tom.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    @Test
    internal fun skalSetteArbeidsgivernavnTilOrgnrHvisArbeidsgiverErOrganisasjon() {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(fnr) } returns listOf(createArbeidsforhold(false, fom, tom))

        val arbeidsforholdList = arbeidsforholdService.hentArbeidsforhold(fnr)
        val arbeidsforhold = arbeidsforholdList!![0]

        assertThat(arbeidsforhold.arbeidsgivernavn).isEqualTo("Privatperson")
        assertThat(arbeidsforhold.orgnr).isNull()
    }

    @Test
    internal fun skalAddereStillingsprosentFraArbeidsavtaler() {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(fnr) } returns listOf(createArbeidsforholdMedFlereArbeidsavtaler(12.3, 45.6))
        every { organisasjonService.hentOrgNavn(any()) } returns orgNavn

        val arbeidsforholdList = arbeidsforholdService.hentArbeidsforhold(fnr)
        val arbeidsforhold = arbeidsforholdList!![0]

        // desimaler strippes fra double til long
        assertThat(arbeidsforhold.fastStillingsprosent).isEqualTo(57L)
    }

    @Test
    internal fun ansettelsesperiodeTomErNull() {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(fnr) } returns listOf(createArbeidsforhold(true, fom, null))
        every { organisasjonService.hentOrgNavn(any()) } returns orgNavn

        val arbeidsforholdList = arbeidsforholdService.hentArbeidsforhold(fnr)
        val arbeidsforhold = arbeidsforholdList!![0]

        assertThat(arbeidsforhold.arbeidsgivernavn).isEqualTo(orgNavn)
        assertThat(arbeidsforhold.harFastStilling).isTrue
        assertThat(arbeidsforhold.fastStillingsprosent).isEqualTo(100L)
        assertThat(arbeidsforhold.orgnr).isEqualTo(orgnr)
        assertThat(arbeidsforhold.fom).isEqualTo(fom.format(DateTimeFormatter.ISO_LOCAL_DATE))
        assertThat(arbeidsforhold.tom).isNull()
    }

    private fun createArbeidsforhold(erArbeidsgiverOrganisasjon: Boolean, fom: LocalDate, tom: LocalDate?): ArbeidsforholdDto {
        val ansettelsesperiodeDto = AnsettelsesperiodeDto(PeriodeDto(fom, tom))
        val arbeidsavtaleDto = ArbeidsavtaleDto(100.0)
        return ArbeidsforholdDto(
            ansettelsesperiodeDto,
            listOf(arbeidsavtaleDto),
            "arbeidsforholdId",
            if (erArbeidsgiverOrganisasjon) arbeidsgiverOrganisasjon else arbeidsgiverPerson,
            arbeidstaker
        )
    }

    private fun createArbeidsforholdMedFlereArbeidsavtaler(vararg stillingsprosenter: Double): ArbeidsforholdDto {
        val ansettelsesperiodeDto = AnsettelsesperiodeDto(PeriodeDto(fom, tom))
        val arbeidsavtaler = stillingsprosenter.map { ArbeidsavtaleDto(it) }
        return ArbeidsforholdDto(
            ansettelsesperiodeDto,
            arbeidsavtaler,
            "arbeidsforholdId",
            arbeidsgiverOrganisasjon,
            arbeidstaker
        )
    }
}
