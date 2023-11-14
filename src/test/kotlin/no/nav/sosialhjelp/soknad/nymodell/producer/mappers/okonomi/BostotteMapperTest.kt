package no.nav.sosialhjelp.soknad.nymodell.producer.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bostotte
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BostotteRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.BostotteMapper
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(BostotteMapper::class)
class BostotteMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var bostotteRepository: BostotteRepository

    @Autowired
    private lateinit var bostotteMapper: BostotteMapper

    @Test
    fun `Map bostotte til JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()
        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }

        createAndSaveBostotte(nySoknad.id)
        bostotteMapper.mapDomainToJson(nySoknad.id, json)

        with (json.soknad.data.okonomi.opplysninger.bostotte) {
            assertThat(saker).hasSize(2)
            assertThat(saker.find { it.status == BostotteStatus.VEDTATT.name } ).isNotNull
            assertThat(saker.find { it.status == BostotteStatus.UNDER_BEHANDLING.name } ).isNotNull
        }
    }

    private fun createAndSaveBostotte(soknadId: UUID) {
        bostotteRepository.saveAll(
            listOf(
                Bostotte(
                    soknadId = soknadId,
                    type = "Utbetaling",
                    status = BostotteStatus.VEDTATT,
                    vedtaksstatus = Vedtaksstatus.INNVILGET
                ),
                Bostotte(
                    soknadId = soknadId,
                    type = "Enda en utbetaling",
                    status = BostotteStatus.UNDER_BEHANDLING,
                )
            )
        )
    }

}