package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.adressesok.AdressesokClient
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.opprettFolkeregistrertAdresse
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet as NyNavEnhet

class KontaktIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

    @MockkBean
    private lateinit var geografiskTilknytningService: GeografiskTilknytningService

    @MockkBean
    private lateinit var norgService: NorgService

    @MockkBean
    private lateinit var adressesokClient: AdressesokClient

    @Test
    fun `Skal returnere alle adresser for soknad`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val kontakt = kontaktRepository.save(opprettKontakt(soknad.id))

        doGet(
            uri = "/soknad/${soknad.id}/adresser",
            responseBodyClass = AdresserDto::class.java,
        ).also {
            assertThat(it.adresseValg).isEqualTo(kontakt.adresser.adressevalg)

            assertThat(it.midlertidigAdresse).isInstanceOf(MatrikkelAdresse::class.java)
            assertThat(it.midlertidigAdresse).isEqualTo(kontakt.adresser.midlertidig)

            assertThat(it.folkeregistrertAdresse).isInstanceOf(VegAdresse::class.java)
            assertThat(it.folkeregistrertAdresse).isEqualTo(kontakt.adresser.folkeregistrert)

            assertThat(it.brukerAdresse).isInstanceOf(UstrukturertAdresse::class.java)
            assertThat(it.brukerAdresse).isEqualTo(kontakt.adresser.fraBruker)
        }
    }

    @Test
    fun `Skal oppdatere brukeradresse i soknad`() {
        val lagretSoknad = opprettSoknad().let { soknadRepository.save(it) }

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.SOKNAD,
                brukerAdresse = opprettFolkeregistrertAdresse(),
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.adresser.adressevalg).isEqualTo(adresserInput.adresseValg)
            assertThat(it.adresser.fraBruker).isEqualTo(adresserInput.brukerAdresse)
        }
    }

    @Test
    fun `Skal oppdatere navenhet for valgt folkeregistrert adresse`() {
        val lagretSoknad = opprettSoknad().let { soknadRepository.save(it) }
        val adresser = Adresser(folkeregistrert = MatrikkelAdresse("1234", "12", "1", null, null, null))
        kontaktRepository.save(opprettKontakt(lagretSoknad.id, adresser = adresser))

        every { geografiskTilknytningService.hentGeografiskTilknytning(userId) } returns "abc"
        val navEnhet = NavEnhet("123", "NAV", "NAV", "123")
        every { norgService.getEnhetForGt("abc") } returns navEnhet

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.FOLKEREGISTRERT,
                brukerAdresse = null,
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.mottaker).isEqualTo(NyNavEnhet("NAV", "123", "1234", "123", "NAV"))
        }
    }

    @Test
    fun `Skal oppdatere navenhet for manuelt innskrevet adresse`() {
        val lagretSoknad = opprettSoknad().let { soknadRepository.save(it) }
        val adresser = Adresser(folkeregistrert = MatrikkelAdresse("1234", "12", "1", null, null, null))
        kontaktRepository.save(opprettKontakt(lagretSoknad.id, adresser = adresser))

        val vegadresse = VegadresseDto("3883", 1, null, "Testveien", "Nav kommune", "1234", "123", "Navstad", null)
        every { adressesokClient.getAdressesokResult(any()) } returns AdressesokResultDto(listOf(AdressesokHitDto(vegadresse, 1F)), 1, 1, 1)
        val navEnhet = NavEnhet("1212", "Sandvika Nav-senter", "Sandvika", "123")
        every { norgService.getEnhetForGt("1234") } returns navEnhet

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.SOKNAD,
                brukerAdresse = VegAdresse(kommunenummer = "12", adresselinjer = listOf("Test 1"), postnummer = "1337", poststed = "Sandvika", gatenavn = "Testveien", husnummer = "1"),
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.mottaker).isEqualTo(NyNavEnhet("Sandvika Nav-senter", "1212", "1234", "123", "Sandvika"))
        }
    }
}
