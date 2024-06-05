package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.service.ForsorgerService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType.BARNEBIDRAG_MOTTAR
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType.BARNEBIDRAG_BETALER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class BarnebidragTest : AbstractOkonomiServiceTest() {
    @Autowired
    private lateinit var forsorgerService: ForsorgerService

    @Test
    fun `Oppdatere forsorger skal oppdatere Inntekt og Utgift, samt forvente dokumentasjon`() {
        forsorgerService.updateForsorger(soknad.id, Barnebidrag.BEGGE, emptyList())

        okonomiRepository.findByIdOrNull(soknad.id)!!.run {
            assertThat(inntekter.toList()).hasSize(1).allMatch { it.type == BARNEBIDRAG_MOTTAR }
            assertThat(utgifter.toList()).hasSize(1).allMatch { it.type == BARNEBIDRAG_BETALER }
        }

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also { doklist ->
            assertThat(doklist)
                .hasSize(2)
                .anyMatch { it.type == BARNEBIDRAG_MOTTAR }
                .anyMatch { it.type == BARNEBIDRAG_BETALER }
        }
    }

    @Test
    fun `Endre verdi Barnebidrag skal fjerne relevant okonomi-post og vedlegg`() {
        forsorgerService.updateForsorger(soknad.id, Barnebidrag.BEGGE, emptyList())
        okonomiRepository.findByIdOrNull(soknad.id)!!
            .run { assertThat(inntekter).allMatch { it.type == BARNEBIDRAG_MOTTAR } }

        forsorgerService.updateForsorger(soknad.id, Barnebidrag.BETALER, emptyList())

        okonomiRepository.findByIdOrNull(soknad.id)!!.run {
            assertThat(inntekter).isEmpty()
            assertThat(utgifter.toList()).hasSize(1).allMatch { it.type == BARNEBIDRAG_BETALER }
        }

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also { doklist ->
            assertThat(doklist)
                .hasSize(1)
                .allMatch { it.type == BARNEBIDRAG_BETALER }
        }
    }

    @Test
    fun `Barnebidrag lik null skal fjerne alle okonomi-poster og dokumentasjonsforventninger`() {
        forsorgerService.updateForsorger(soknad.id, Barnebidrag.BEGGE, emptyList())
        okonomiRepository.findByIdOrNull(soknad.id)!!
            .run { assertThat(inntekter).allMatch { it.type == BARNEBIDRAG_MOTTAR } }

        forsorgerService.updateForsorger(soknad.id, null, emptyList())

        okonomiRepository.findByIdOrNull(soknad.id)!!.run {
            assertThat(inntekter).isEmpty()
            assertThat(utgifter).isEmpty()
        }

        assertThat(dokumentasjonRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }
}
