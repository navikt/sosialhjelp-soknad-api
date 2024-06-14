package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.StudielanDto
import no.nav.sosialhjelp.soknad.v2.livssituasjon.StudielanInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class StudielanIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiRepository: OkonomiRepository

    @Autowired
    private lateinit var livssituasjonRepository: LivssituasjonRepository

    @Test
    fun `Hent studielan skal returnere korrekte date`() {
        val soknad = soknadRepository.save(opprettSoknad())
        livssituasjonRepository.save(Livssituasjon(soknadId = soknad.id, utdanning = Utdanning(erStudent = true)))

        val okonomi =
            Okonomi(
                soknadId = soknad.id,
                bekreftelser = setOf(Bekreftelse(type = BekreftelseType.STUDIELAN_BEKREFTELSE, verdi = true)),
                inntekter = setOf(Inntekt(type = InntektType.STUDIELAN_INNTEKT)),
            )
                .also { okonomiRepository.save(it) }

        doGet(
            uri = getStudielanUrl(soknad.id),
            responseBodyClass = StudielanDto::class.java,
        )
            .also { dto ->
                assertThat(dto.erStudent).isTrue()
                assertThat(okonomi.bekreftelser.toList()).hasSize(1)
                    .allMatch { it.type == BekreftelseType.STUDIELAN_BEKREFTELSE }
                    .allMatch { it.verdi == dto.mottarStudielan }
                assertThat(okonomi.inntekter.toList()).hasSize(1).allMatch { it.type == InntektType.STUDIELAN_INNTEKT }
            }
    }

    @Test
    fun `Hvis erStudent er false skal mottarStudielan = null`() {
        val soknad = soknadRepository.save(opprettSoknad())
        livssituasjonRepository.save(Livssituasjon(soknadId = soknad.id, utdanning = Utdanning(erStudent = false)))

        doGet(
            uri = getStudielanUrl(soknad.id),
            responseBodyClass = StudielanDto::class.java,
        )
            .also {
                assertThat(it.erStudent).isFalse()
                assertThat(it.mottarStudielan).isNull()
            }
    }

    @Test
    fun `Oppdatere studielan skal lagres i db`() {
        val soknad = soknadRepository.save(opprettSoknad())
        livssituasjonRepository.save(Livssituasjon(soknadId = soknad.id, utdanning = Utdanning(erStudent = true)))

        doPut(
            uri = getStudielanUrl(soknad.id),
            requestBody = StudielanInput(mottarStudielan = true),
            responseBodyClass = StudielanDto::class.java,
            soknadId = soknad.id,
        )
            .also { response ->
                assertThat(response.erStudent).isTrue()
                assertThat(response.mottarStudielan).isTrue()
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.bekreftelser.toList()).hasSize(1)
                .allMatch { it.type == BekreftelseType.STUDIELAN_BEKREFTELSE && it.verdi }
            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .allMatch { it.type == InntektType.STUDIELAN_INNTEKT }
        }
    }

    @Test
    fun `Oppdatere studielan med erStudent = false skal ignoreres`() {
        val soknad = soknadRepository.save(opprettSoknad())
        livssituasjonRepository.save(Livssituasjon(soknadId = soknad.id, utdanning = Utdanning(erStudent = false)))

        doPut(
            uri = getStudielanUrl(soknad.id),
            requestBody = StudielanInput(mottarStudielan = true),
            responseBodyClass = StudielanDto::class.java,
            soknadId = soknad.id,
        )
            .also { response ->
                assertThat(response.erStudent).isFalse()
                assertThat(response.mottarStudielan).isNull()
            }

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)).isNull()
    }

    companion object {
        private fun getStudielanUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/studielan"
    }
}
