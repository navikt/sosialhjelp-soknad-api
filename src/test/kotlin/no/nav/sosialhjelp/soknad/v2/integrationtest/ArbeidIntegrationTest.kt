package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormelt
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormeltRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.ArbeidDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.ArbeidInput
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataFormelt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.*

class ArbeidIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataFormeltRepository: BrukerdataFormeltRepository

    private fun getPath(soknadId: UUID): String {
        return "/soknad/$soknadId/arbeid"
    }

    @Test
    fun `Hente arbeid skal returnere korrekte data`() {

        val soknad = soknadRepository.save(createSoknad())
        val brukerdataFormelt = brukerdataFormeltRepository.save(opprettBrukerdataFormelt(soknad.id!!))

        val arbeidDto = doGet(
            getPath(soknad.id!!),
            ArbeidDto::class.java
        )

        assertThat(arbeidDto.kommentar).isEqualTo(brukerdataFormelt.kommentarArbeidsforhold)

        arbeidDto.arbeidsforholdList.forEachIndexed { index, arbeidsforholdDto ->
            with(soknad.arbeidsForhold[index]) {
                assertThat(arbeidsforholdDto.arbeidsgivernavn).isEqualTo(arbeidsgivernavn)
                assertThat(arbeidsforholdDto.orgnummer).isEqualTo(orgnummer)
                assertThat(arbeidsforholdDto.start).isEqualTo(start)
                assertThat(arbeidsforholdDto.slutt).isEqualTo(slutt)
                assertThat(arbeidsforholdDto.harFastStilling).isEqualTo(harFastStilling)
                assertThat(arbeidsforholdDto.fastStillingsprosent).isEqualTo(fastStillingsprosent)
            }
        }
    }

    @Test
    fun `Oppdatere arbeid skal oppdatere databasen`() {
        val soknad = soknadRepository.save(createSoknad())
        brukerdataFormeltRepository.save(BrukerdataFormelt(soknadId = soknad.id!!))

        val input = ArbeidInput("Jeg synes ikke arbeid er så gøy lenger")

        doPut(
            getPath(soknad.id!!),
            input,
            ArbeidDto::class.java
        ).also {
            assertThat(it.kommentar).isEqualTo(input.kommentarTilArbeidsforhold)
        }

        brukerdataFormeltRepository.findByIdOrNull(soknad.id!!)?.let {
            assertThat(it.kommentarArbeidsforhold).isEqualTo(input.kommentarTilArbeidsforhold)
        }
    }

    @Test
    fun `Bruke tekst med ugyldige tegn skal gi feilmelding`() {
        val soknad = soknadRepository.save(createSoknad())

        doPutExpectError(
            getPath(soknad.id!!),
            ArbeidInput("En ny kommentar med rare tegn !#%&#¤&&"),
            HttpStatus.BAD_REQUEST
        )
    }
}
