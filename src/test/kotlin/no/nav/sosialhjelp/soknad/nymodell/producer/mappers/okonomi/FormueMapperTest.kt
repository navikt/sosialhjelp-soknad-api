package no.nav.sosialhjelp.soknad.nymodell.producer.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_BRUKSKONTO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_SPAREKONTO
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.FormueMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(FormueMapper::class)
class FormueMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var formueRepository: FormueRepository

    @Autowired
    private lateinit var formueMapper: FormueMapper

    @Test
    fun `Map Formue to JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()
        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }

        createAndSaveFormue(nySoknad.id)
        formueMapper.mapDomainToJson(nySoknad.id, json)

        with (json.soknad.data.okonomi.oversikt) {
            assertThat(formue).hasSize(2)
            assertThat(formue.find { it.type == KONTOOVERSIKT_BRUKSKONTO.toSoknadJsonType() }).isNotNull
            assertThat(formue.find { it.type == KONTOOVERSIKT_SPAREKONTO.toSoknadJsonType() }).isNotNull
        }
    }

    private fun createAndSaveFormue(soknadId: UUID) {
        formueRepository.saveAll(
            listOf(
                Formue(
                    soknadId = soknadId,
                    type = KONTOOVERSIKT_BRUKSKONTO,
                ),
                Formue(
                    soknadId = soknadId,
                    type = KONTOOVERSIKT_SPAREKONTO,
                )
            )
        )
    }
}
