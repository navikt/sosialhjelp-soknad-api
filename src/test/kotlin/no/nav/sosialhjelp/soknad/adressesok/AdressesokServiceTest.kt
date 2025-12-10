package no.nav.sosialhjelp.soknad.adressesok

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AdressesokServiceTest {
    private val adressesokClient = mockk<AdressesokClient>()
    private val kodeverkService = mockk<KodeverkService>()

    private val adressesokService = AdressesokService(adressesokClient, kodeverkService)

    private val resultDto =
        AdressesokResultDto(
            hits = emptyList(),
            pageNumber = 1,
            totalPages = 1,
            totalHits = 0,
        )

    private val defaultVegadresse =
        VegadresseDto(
            matrikkelId = "matrikkelId",
            husnummer = 1,
            husbokstav = "B",
            adressenavn = "Testveien",
            kommunenavn = "Oslo",
            kommunenummer = "0301",
            postnummer = "0123",
            poststed = "Oslo",
            bydelsnummer = null,
        )

    @Test
    fun `sokEtterAdresser skal returnere tom liste ved for kort sokestreng`() {
        assertThat(adressesokService.sokEtterAdresser("")).isEmpty()
        assertThat(adressesokService.sokEtterAdresser(" ")).isEmpty()
        assertThat(adressesokService.sokEtterAdresser("a")).isEmpty()
        assertThat(adressesokService.sokEtterAdresser("a 2")).isEmpty()
    }

    @Test
    fun `sokEtterAdresser skal returnere tom liste ved ingen treff i PDL`() {
        every { adressesokClient.getAdressesokResult(any()) } returns resultDto.copy(hits = null)
        assertThat(adressesokService.sokEtterAdresser("Oslogaten 2")).isEmpty()
    }

    @Test
    fun `sokEtterAdresser skal returnere funn fra PDL`() {
        every { adressesokClient.getAdressesokResult(any()) } returns
            resultDto.copy(
                hits =
                    listOf(
                        AdressesokHitDto(
                            vegadresse = defaultVegadresse,
                            score = 1.0f,
                        ),
                    ),
                pageNumber = 1,
                totalPages = 1,
                totalHits = 1,
            )

        val result = adressesokService.sokEtterAdresser("oslogaten 42, 1337 Leet")

        assertThat(result).hasSize(1)
        assertThat(result.first().kommunenummer).isEqualTo("0301")
    }
}
