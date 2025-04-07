package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.createSituasjonsendring
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringDto
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class SituasjonsendringIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var situasjonsendringRepository: SituasjonsendringRepository

    private lateinit var storedSoknad: Soknad

    @BeforeEach
    fun setUp() {
        storedSoknad = soknadRepository.save(opprettSoknad(id = soknadId))
    }

    @Test
    fun `Put på situasjonsendring skal oppdatere situasjonsendring med data fra dto`() {
        situasjonsendringRepository.save(
            createSituasjonsendring(storedSoknad.id, null, null),
        )

        val situasjonsendringDto = SituasjonsendringDto("Noe har endret seg", true)
        doPut("/soknad/${storedSoknad.id}/situasjonsendring", situasjonsendringDto, Unit::class.java, storedSoknad.id)

        situasjonsendringRepository.findByIdOrNull(storedSoknad.id)?.let {
            assertThat(it.hvaErEndret).isEqualTo("Noe har endret seg")
            assertThat(it.endring).isEqualTo(true)
        }
    }

    @Test
    fun `Get på situasjonsendring skal returnere riktig situasjonsendring`() {
        situasjonsendringRepository.save(
            createSituasjonsendring(storedSoknad.id, "abc", true),
        )

        val situasjonsendring = doGet("/soknad/${storedSoknad.id}/situasjonsendring", SituasjonsendringDto::class.java)

        with(situasjonsendring) {
            assertThat(hvaErEndret).isEqualTo("abc")
            assertThat(endring).isEqualTo(true)
        }
    }
}
