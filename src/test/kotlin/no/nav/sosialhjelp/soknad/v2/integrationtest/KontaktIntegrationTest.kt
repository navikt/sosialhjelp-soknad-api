package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
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

class KontaktIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

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
}
