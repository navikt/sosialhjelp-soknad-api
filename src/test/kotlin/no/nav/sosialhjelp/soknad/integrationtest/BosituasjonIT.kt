package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BosituasjonDto
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.BosituasjonRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Botype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import java.util.*
import kotlin.jvm.optionals.getOrNull
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

class BosituasjonIT: SoknadApiIntergrationTest() {

    @Autowired
    private lateinit var bosituasjonRepository: BosituasjonRepository

    @Test
    fun `Legg til ny Bosituasjon`() {
        val soknadId = startSoknad()

        val botype = Botype.EIER
        val antallPersoner = 4

        val response = doUpdate(
            soknadId, botype, antallPersoner
        )
        assertThat(response.statusCode.value()).isEqualTo(200)

        bosituasjonRepository.findById(soknadId).getOrNull()?.let {
            assertThat(it.botype).isEqualTo(botype)
            assertThat(it.antallPersoner).isEqualTo(antallPersoner)
        } ?: throw IkkeFunnetException("Bosituasjon finnes ikke")
    }

    @Test
    fun `Endre Bosituasjon`() {
        val soknadId = startSoknad()
        val bosituasjon = Bosituasjon(soknadId, Botype.EIER, 8)
        bosituasjonRepository.save(bosituasjon)

        doUpdate(soknadId, Botype.FAMILIE, 5)

        val response = doGet(soknadId)
        assertThat(response.statusCode.value()).isEqualTo(200)

        val bosituasjonDto = response.body!!
        assertThat(bosituasjonDto.botype).isEqualTo(Botype.FAMILIE)
        assertThat(bosituasjonDto.antallPersoner).isEqualTo(5)

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadId.toString(), eier())
        assertThat(soknadUnderArbeid.behandlingsId).isEqualTo(soknadId.toString())

        // TODO koble på MergeJsonInternalSoknadService
    }

    @Test
    fun `Bosituasjon finnes ikke`() {
        val soknadId = startSoknad()

        val response = doGet(soknadId)
        assertThat(response.statusCode.value()).isEqualTo(404)
    }

    private fun startSoknad() = oldSoknadService.startSoknad().let {
        val soknadUuid = UUID.fromString(it)
        assertThat(soknadRepository.existsById(soknadUuid)).isTrue()
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUuid.toString(), eier())).isNotNull
        soknadUuid
    }

    private fun doUpdate(soknadId: UUID, botype: Botype, antallPersoner: Int): ResponseEntity<BosituasjonDto> {
        return restTemplate.exchange(
            "/soknad/$soknadId/bosituasjon",
            HttpMethod.PUT,
            HttpEntity(BosituasjonDto(botype, antallPersoner)),
            BosituasjonDto::class.java
        )
    }

    private fun doGet(soknadId: UUID): ResponseEntity<BosituasjonDto> =
        restTemplate.getForEntity("/soknad/$soknadId/bosituasjon", BosituasjonDto::class.java)

//    fun putBosituasjon(botype: String, antallPersoner: Int) {
//        restTemplate.put(
//            "/soknad/{soknadId}/bosituasjon",
//            BosituasjonDto(botype, antallPersoner),
//            mapOf("soknadId" to soknadId)
//        )
//    }
}