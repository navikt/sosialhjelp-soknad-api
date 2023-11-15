package no.nav.sosialhjelp.soknad.nymodell.producer.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.LONNSLIPP_ARBEID
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.SALGSOPPGJOR_EIENDOM
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.InntektMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(InntektMapper::class)
class InntektMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var inntektRepository: InntektRepository

    @Autowired
    private lateinit var inntektMapper: InntektMapper

    @Test
    fun `Map Inntekt to JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()
        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }

        createAndSaveInntekt(nySoknad.id)
        inntektMapper.mapDomainToJson(nySoknad.id, json)

        with (json.soknad.data.okonomi) {
            opplysninger.let {
                assertThat(it.utbetaling).hasSize(1)
                assertThat(
                    it.utbetaling.find { utb -> utb.type == SALGSOPPGJOR_EIENDOM.toSoknadJsonType() }
                ).isNotNull
            }
            oversikt.let {
                assertThat(it.inntekt).hasSize(1)
                assertThat(
                    it.inntekt.find { inn -> inn.type == LONNSLIPP_ARBEID.toSoknadJsonType() }
                ).isNotNull
            }
        }
    }

    private fun createAndSaveInntekt(soknadId: UUID) {
        inntektRepository.saveAll(
            listOf(
                Inntekt(
                    soknadId = soknadId,
                    type = LONNSLIPP_ARBEID,
                ),
                Inntekt(
                    soknadId = soknadId,
                    type = SALGSOPPGJOR_EIENDOM,
                    utbetaling = Utbetaling(
                        kilde = Kilde.BRUKER,
                        belop = 14500
                    )
                )
            )
        )
    }
}