package no.nav.sosialhjelp.soknad.organisasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.client.organisasjon.OrganisasjonClient
import no.nav.sosialhjelp.soknad.client.organisasjon.dto.NavnDto
import no.nav.sosialhjelp.soknad.client.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OrganisasjonServiceTest {

    private val organisasjonClient: OrganisasjonClient = mockk()
    private val organisasjonService = OrganisasjonService(organisasjonClient)

    private val orgnr = "12345"

    @Test
    internal fun `skal hente orgnavn med null i navnelinjer`() {
        every { organisasjonClient.hentOrganisasjonNoekkelinfo(any()) } returns OrganisasjonNoekkelinfoDto(
            NavnDto("Testesen A/S", "andre linje", null, null, null),
            orgnr
        )

        val orgnavn = organisasjonService.hentOrgNavn(orgnr)

        assertThat(orgnavn).isEqualTo("Testesen A/S, andre linje")
    }

    @Test
    internal fun `skal hente orgNavn med tomme strings i navnelinjer`() {
        every { organisasjonClient.hentOrganisasjonNoekkelinfo(any()) } returns OrganisasjonNoekkelinfoDto(
            NavnDto("Testesen A/S", "andre linje", "", "", ""),
            orgnr
        )

        val orgNavn: String = organisasjonService.hentOrgNavn(orgnr)

        assertThat(orgNavn).isEqualTo("Testesen A/S, andre linje")
    }
}
