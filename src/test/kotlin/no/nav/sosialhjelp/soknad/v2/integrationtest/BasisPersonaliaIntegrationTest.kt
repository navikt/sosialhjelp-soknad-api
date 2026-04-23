package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.PersonaliaDto
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

class BasisPersonaliaIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun `Basis personalia returnerer felt uten navn`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))
        eierRepository.save(Eier(soknad.id, "Norsk", true, Navn("Mons", null, "Monsen")))
        val result = doGet("/soknad/${soknad.id}/personalia/basisPersonalia", PersonaliaDto::class.java)
        assertThat(result.fodselsnummer).isEqualTo(userId)
        assertThat(result.statsborgerskap).isEqualTo("Norsk")
        assertThat(result.nordiskBorger).isTrue()
    }
}
