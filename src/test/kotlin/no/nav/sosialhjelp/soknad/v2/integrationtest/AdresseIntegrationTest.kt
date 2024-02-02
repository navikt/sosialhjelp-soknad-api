package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.adresse.AdresseRepository
import no.nav.sosialhjelp.soknad.v2.adresse.AdresserDto
import no.nav.sosialhjelp.soknad.v2.adresse.AdresserInput
import no.nav.sosialhjelp.soknad.v2.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.toAdresserDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettAdresserSoknad
import no.nav.sosialhjelp.soknad.v2.opprettFolkeregistrertAdresse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class AdresseIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var adresseRepository: AdresseRepository

    @Test
    fun `Hente ut adresser for soknad`() {

        val lagretSoknad = createSoknad().let { soknadRepository.save(it) }
        val adresserDto = opprettAdresserSoknad(soknadId = lagretSoknad.id!!)
            .let {
                val adresser = adresseRepository.save(it)
                adresser.toAdresserDto()
            }

        doGet(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            responseBodyClass = AdresserDto::class.java
        ).also {
            assertThat(it).isEqualTo(adresserDto)
            assertThat(it.midlertidigAdresse).isInstanceOf(UstrukturertAdresse::class.java)
            assertThat(it.folkeregistrertAdresse).isInstanceOf(VegAdresse::class.java)
            assertThat(it.adresseBruker).isInstanceOf(MatrikkelAdresse::class.java)
        }
    }

    @Test
    fun `Oppdatere brukeradresse`() {
        val lagretSoknad = createSoknad().let { soknadRepository.save(it) }

        val adresserInput = AdresserInput(
            valgtAdresse = AdresseValg.SOKNAD,
            adresseBruker = opprettFolkeregistrertAdresse()
        )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java
        )

        adresseRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.brukerInput!!.valgtAdresse).isEqualTo(adresserInput.valgtAdresse)
            assertThat(it.brukerInput!!.brukerAdresse!!).isEqualTo(adresserInput.adresseBruker!!)
        }
    }
}
