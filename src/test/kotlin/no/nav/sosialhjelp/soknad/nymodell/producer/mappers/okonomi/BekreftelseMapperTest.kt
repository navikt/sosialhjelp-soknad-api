package no.nav.sosialhjelp.soknad.nymodell.producer.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.*
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.BekreftelseMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(BekreftelseMapper::class)
class BekreftelseMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var bekreftelseRepository: BekreftelseRepository

    @Autowired
    private lateinit var bekreftelseMapper: BekreftelseMapper


    @Test
    fun `Map Bekreftelse to JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()
        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }

        createAndSaveBekreftelse(nySoknad.id)
        bekreftelseMapper.mapDomainToJson(nySoknad.id, json)

        with (json.soknad.data.okonomi.opplysninger) {
            assertThat(bekreftelse).hasSize(2)
            assertThat(bekreftelse.filter { it.type == BOSTOTTE.toSoknadJsonType() }).hasSize(1)
            assertThat(bekreftelse.filter { it.type == STUDIELAN.toSoknadJsonType() }).hasSize(1)
        }
    }

    private fun createAndSaveBekreftelse(soknadId: UUID) {
        bekreftelseRepository.saveAll(
            listOf(
                Bekreftelse(
                    soknadId = soknadId,
                    type = BOSTOTTE
                ),
                Bekreftelse(
                    soknadId = soknadId,
                    type = STUDIELAN
                )
            )
        )
    }
}
