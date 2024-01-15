package no.nav.sosialhjelp.soknad.organisasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.organisasjon.dto.NavnDto
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OrganisasjonServiceTest {

    private val organisasjonClient: OrganisasjonClient = mockk()
    private val organisasjonService = OrganisasjonService(organisasjonClient)

    private val orgnr = "12345"

    private val dto = OrganisasjonNoekkelinfoDto(
        navn = NavnDto(
            navnelinje1 = "Testesen A/S",
            navnelinje2 = "andre linje",
            navnelinje3 = null,
            navnelinje4 = null,
            navnelinje5 = null,
        ),
        organisasjonsnummer = orgnr,
    )

    @Test
    internal fun `skal hente orgnavn med null i navnelinjer`() {
        every { organisasjonClient.hentOrganisasjonNoekkelinfo(any()) } returns dto

        val orgnavn = organisasjonService.hentOrgNavn(orgnr)

        assertThat(orgnavn).isEqualTo("Testesen A/S, andre linje")
    }

    @Test
    internal fun `skal hente orgNavn med tomme strings i navnelinjer`() {
        every { organisasjonClient.hentOrganisasjonNoekkelinfo(any()) } returns dto.copy(navn = NavnDto("Testesen A/S", "andre linje", "", "", ""))

        val orgNavn: String = organisasjonService.hentOrgNavn(orgnr)

        assertThat(orgNavn).isEqualTo("Testesen A/S, andre linje")
    }

    @Test
    fun skalReturnereOrganisasjonOmGyldigOrganisasjonsnummer() {
        val organisasjonsnummer = "089640782"
        val result = organisasjonService.mapToJsonOrganisasjon(organisasjonsnummer)
        assertThat(result).isNotNull
        assertThat(result!!.organisasjonsnummer).isEqualTo(organisasjonsnummer)
    }

    @Test
    fun skalReturnereNullOmOrganisasjonsnummerInneholderTekst() {
        val organisasjonsnummer = "o89640782"
        val result = organisasjonService.mapToJsonOrganisasjon(organisasjonsnummer)
        assertThat(result).isNull()
    }

    @Test
    fun skalReturnereNullOmForKortOrganisasjonsnummer() {
        val nummer = "12345678"
        val result = organisasjonService.mapToJsonOrganisasjon(nummer)
        assertThat(result).isNull()
    }

    @Test
    fun skalReturnereNullOmForLangtOrganisasjonsnummer() {
        val nummer = "1234567890"
        val result = organisasjonService.mapToJsonOrganisasjon(nummer)
        assertThat(result).isNull()
    }

    @Test
    fun skalReturnereNullVedPersonnummerSomOrganisasjonsnummer() {
        val personnummer = "01010011111"
        val result = organisasjonService.mapToJsonOrganisasjon(personnummer)
        assertThat(result).isNull()
    }
}
