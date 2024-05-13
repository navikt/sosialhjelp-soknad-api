package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.service.ForsorgerService
import no.nav.sosialhjelp.soknad.v2.familie.service.SivilstandService
import no.nav.sosialhjelp.soknad.v2.register.handlers.AbstractPersonHandlerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired

class FamilieHandlerTest : AbstractPersonHandlerTest() {
    @Autowired
    private lateinit var forsorgerService: ForsorgerService

    @Autowired
    private lateinit var sivilstandService: SivilstandService

    @Test
    fun `Hente person skal hente og lagre familie-data`() {
        val familieDtos = createAnswerForPersonMedEktefelleOgBarn()
        fetchPerson.fetchAndSave(soknadId = soknad.id)

        forsorgerService.findForsorger(soknadId = soknad.id)?.let {
            assertThat(it.ansvar.size).isEqualTo(familieDtos.barn.size)
        }
            ?: fail("Fant ikke Forsorger-objekt")

        sivilstandService.findSivilstand(soknadId = soknad.id)?.let {
            assertThat(it.sivilstatus).isEqualTo(Sivilstatus.GIFT)
        }
            ?: fail("Fant ikke Sivilstand-objekt")
    }
}
