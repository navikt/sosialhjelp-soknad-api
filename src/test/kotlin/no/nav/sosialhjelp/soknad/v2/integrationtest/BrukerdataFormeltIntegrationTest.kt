package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormeltRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPersonligRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.toBegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataPersonlig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class BrukerdataFormeltIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataFormeltRepository: BrukerdataFormeltRepository

    @Autowired
    private lateinit var brukerdataPersonligRepository: BrukerdataPersonligRepository

    @Test
    fun `Skal returnere begrunnelse fra soknad`() {
        val lagretSoknad = createSoknad().let { soknadRepository.save(it) }

        val brukerdataPersonlig = opprettBrukerdataPersonlig(lagretSoknad.id!!)
            .let { brukerdataPersonligRepository.save(it) }

        doGet(
            uri = "/soknad/${lagretSoknad.id}/begrunnelse",
            responseBodyClass = BegrunnelseDto::class.java
        ).also {
            assertThat(it).isEqualTo(brukerdataPersonlig.toBegrunnelseDto())
        }
    }

    @Test
    fun `Skal oppdatere begrunnelse i soknad`() {
        val lagretSoknad = createSoknad().let { soknadRepository.save(it) }

        val begrunnelseDto = BegrunnelseDto(
            hvaSokesOm = "Veldig mye gryn",
            hvorforSoke = "Fordi jeg ikke har mye gryn"
        )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/begrunnelse",
            requestBody = begrunnelseDto,
            responseBodyClass = BegrunnelseDto::class.java
        )

        brukerdataPersonligRepository.findByIdOrNull(lagretSoknad.id)?.let {
            assertThat(it.toBegrunnelseDto()).isEqualTo(begrunnelseDto)
        }
            ?: throw RuntimeException("Brukerdata ble ikke lagret.")
    }
}
