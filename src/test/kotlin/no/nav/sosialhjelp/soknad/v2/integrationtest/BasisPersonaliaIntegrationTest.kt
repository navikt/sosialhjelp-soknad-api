package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.PersonaliaDto
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BasisPersonaliaIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var eierRepository: EierRepository

    @Test
    fun `Fullt navn skal slå sammen navn`() {
        val soknad = soknadRepository.save(opprettSoknad())
        eierRepository.save(Eier(soknad.id, "Norsk", true, Navn("Mons", null, "Monsen")))
        val result = doGet("/soknad/${soknad.id}/personalia/basisPersonalia", PersonaliaDto::class.java)
        assertThat(result.navn.fulltNavn).isEqualTo("Mons Monsen")
    }

    @Test
    fun `Fullt navn skal slå sammen navn med mellomnavn`() {
        val soknad = soknadRepository.save(opprettSoknad())
        eierRepository.save(Eier(soknad.id, "Norsk", true, Navn("Mons", "Johan", "Monsen")))
        val result = doGet("/soknad/${soknad.id}/personalia/basisPersonalia", PersonaliaDto::class.java)
        assertThat(result.navn.fulltNavn).isEqualTo("Mons Johan Monsen")
    }

    @Test
    fun `Fullt navn skal slå sammen navn uten ekstra mellomrom`() {
        val soknad = soknadRepository.save(opprettSoknad())
        eierRepository.save(Eier(soknad.id, "Norsk", true, Navn("   Mons  ", "        Johan", "Monsen\t")))
        val result = doGet("/soknad/${soknad.id}/personalia/basisPersonalia", PersonaliaDto::class.java)
        assertThat(result.navn.fulltNavn).isEqualTo("Mons Johan Monsen")
    }
}
