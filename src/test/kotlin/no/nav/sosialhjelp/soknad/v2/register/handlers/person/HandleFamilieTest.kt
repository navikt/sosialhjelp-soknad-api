package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.register.handlers.AbstractHandlePersonTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired

class HandleFamilieTest: AbstractHandlePersonTest() {


    @Autowired
    private lateinit var familieService: FamilieService

    @Test
    fun `Hente person skal hente og lagre familie-data`() {
        val familieDtos = createAnswerForPersonMedEktefelleOgBarn()
        handlePerson.handle(soknadId = soknad.id)

        familieService.findFamilie(soknadId = soknad.id)?.let {

            assertThat(it.sivilstatus).isEqualTo(Sivilstatus.GIFT)

        }
            ?: fail("Fant ikke Familie-objekt")

    }
}
