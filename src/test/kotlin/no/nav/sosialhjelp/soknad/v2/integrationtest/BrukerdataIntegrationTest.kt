package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.toBegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class BrukerdataIntegrationTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataRepository: BrukerdataRepository

    @Test
    fun `Hente begrunnelse`() {
        val lagretSoknad = createSoknad().let { soknadRepository.save(it) }
        val brukerdata = opprettBrukerdata(soknadId = lagretSoknad.id!!).let { brukerdataRepository.save(it) }

        doGet(
            uri = "/soknad/${lagretSoknad.id}/begrunnelse",
            responseBodyClass = BegrunnelseDto::class.java
        ).also {
            assertThat(it).isEqualTo(brukerdata.toBegrunnelseDto())
        }
    }

    @Test
    fun `Oppdatere begrunnelse`() {
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

        brukerdataRepository.findByIdOrNull(lagretSoknad.id)?.let {
            assertThat(it.toBegrunnelseDto()).isEqualTo(begrunnelseDto)
        }
            ?: throw RuntimeException("Brukerdata ble ikke lagret.")
    }
}
