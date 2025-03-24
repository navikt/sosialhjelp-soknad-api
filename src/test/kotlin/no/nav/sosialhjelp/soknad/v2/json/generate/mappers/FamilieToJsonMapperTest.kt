package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.FamilieToJsonMapper
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator as validator

class FamilieToJsonMapperTest : AbstractMapperTest() {
    private val mapper = FamilieToJsonMapper.Mapper

    @Test
    fun `Status Gift uten ektefelle skal kaste valideringsfeil`() {
        skipAfterEach = true

        Familie(
            soknadId = UUID.randomUUID(),
            sivilstatus = Sivilstatus.GIFT,
        )
            .also { mapper.doMapping(it, json) }

        objectMapper.writeValueAsString(json.soknad)
            .also {
                assertThatThrownBy { validator.ensureValidSoknad(it) }
                    .isInstanceOf(JsonSosialhjelpValidationException::class.java)
            }
    }

    @Test
    fun `Status gift uten ektefelle skal gi Ektefelle med tomt navn`() {
    }
}
