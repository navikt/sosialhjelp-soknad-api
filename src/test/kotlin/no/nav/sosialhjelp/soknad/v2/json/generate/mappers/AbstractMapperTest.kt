package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator as validator

abstract class AbstractMapperTest {
    protected val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
    protected lateinit var json: JsonInternalSoknad

    protected var skipAfterEach = false

    @BeforeEach
    fun setup() {
        json = createValidEmptyJsonInternalSoknad()
    }

    @AfterEach
    fun tearDown() {
        if (!skipAfterEach) {
            objectMapper.writeValueAsString(json.soknad)
                .also { validator.ensureValidSoknad(it) }
        }
    }
}
