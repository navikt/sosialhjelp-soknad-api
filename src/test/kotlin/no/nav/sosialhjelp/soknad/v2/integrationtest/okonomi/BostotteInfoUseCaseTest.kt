package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgifterDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Integrasjonstatus
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class BostotteInfoUseCaseTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var integrasjonstatusRepository: IntegrasjonstatusRepository

    @Test
    fun `Hente bostotte feilet-scenario`() {
        integrasjonstatusRepository.save(Integrasjonstatus(soknad.id, feilStotteHusbanken = true))

        assertSkalVise(false)
    }

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(opprettSoknad(id = soknadId))
    }

    private fun assertSkalVise(vise: Boolean) {
        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = BoutgifterDto::class.java,
        )
            .also { dto -> assertThat(dto.skalViseInfoVedBekreftelse == vise).isTrue() }
    }

    companion object {
        private fun getUrl(soknadId: UUID): String {
            return "/soknad/$soknadId/utgifter/boutgifter"
        }
    }
}
