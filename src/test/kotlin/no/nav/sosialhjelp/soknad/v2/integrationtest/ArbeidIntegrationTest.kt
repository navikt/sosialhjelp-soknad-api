package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidDto
import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class ArbeidIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var livssituasjonRepository: LivssituasjonRepository

    private fun getPath(soknadId: UUID): String {
        return "/soknad/$soknadId/arbeid"
    }

    @Test
    fun `Hente arbeid skal returnere korrekte data`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val livssituasjon = livssituasjonRepository.save(opprettLivssituasjon(soknad.id))

        val arbeidDto = doGet(
            getPath(soknad.id),
            ArbeidDto::class.java
        )

        livssituasjon.arbeid?.let {
            assertThat(arbeidDto.kommentar).isEqualTo(it.kommentar)

            arbeidDto.arbeidsforholdList.forEachIndexed { index, arbeidsforholdDto ->
                with(it.arbeidsforhold[index]) {
                    assertThat(arbeidsforholdDto.arbeidsgivernavn).isEqualTo(arbeidsgivernavn)
                    assertThat(arbeidsforholdDto.orgnummer).isEqualTo(orgnummer)
                    assertThat(arbeidsforholdDto.start).isEqualTo(start)
                    assertThat(arbeidsforholdDto.slutt).isEqualTo(slutt)
                    assertThat(arbeidsforholdDto.harFastStilling).isEqualTo(harFastStilling)
                    assertThat(arbeidsforholdDto.fastStillingsprosent).isEqualTo(fastStillingsprosent)
                }
            }
        }
            ?: fail("Arbeid er null")
    }

    @Test
    fun `Oppdatere arbeid skal oppdatere databasen`() {
        val soknad = soknadRepository.save(opprettSoknad())
        livssituasjonRepository.save(Livssituasjon(soknadId = soknad.id))

        val input = ArbeidInput("Jeg synes ikke arbeid er så gøy lenger")

        doPut(
            getPath(soknad.id),
            input,
            ArbeidDto::class.java,
            soknad.id
        ).also {
            assertThat(it.kommentar).isEqualTo(input.kommentarTilArbeidsforhold)
        }

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.arbeid!!.kommentar).isEqualTo(input.kommentarTilArbeidsforhold)
        }
    }
}
