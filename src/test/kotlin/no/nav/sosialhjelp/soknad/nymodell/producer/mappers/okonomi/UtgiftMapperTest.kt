package no.nav.sosialhjelp.soknad.nymodell.producer.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.ANDRE_UTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.BARNEBIDRAG_BETALER
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.UtgiftMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(UtgiftMapper::class)
class UtgiftMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var utgiftRepository: UtgiftRepository

    @Autowired
    private lateinit var utgiftMapper: UtgiftMapper

    @Test
    fun `Map Utgift to JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()
        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }

        createAndSaveUtgift(nySoknad.id)
        utgiftMapper.mapDomainToJson(nySoknad.id, json)

        with (json.soknad.data.okonomi) {
            opplysninger.let {
                Assertions.assertThat(it.utgift).hasSize(1)
                Assertions.assertThat(
                    it.utgift.find { utg -> utg.type == ANDRE_UTGIFTER.toSoknadJsonType() }
                ).isNotNull
            }
            oversikt.let {
                Assertions.assertThat(it.utgift).hasSize(1)
                Assertions.assertThat(
                    it.utgift.find { utg -> utg.type == BARNEBIDRAG_BETALER.toSoknadJsonType() }
                ).isNotNull
            }
        }
    }

    private fun createAndSaveUtgift(soknadId: UUID) {
        utgiftRepository.saveAll(
            listOf(
                Utgift(
                    soknadId = soknadId,
                    type = ANDRE_UTGIFTER,
                ),
                Utgift(
                    soknadId = soknadId,
                    type = BARNEBIDRAG_BETALER,
                )
            )
        )
    }
}