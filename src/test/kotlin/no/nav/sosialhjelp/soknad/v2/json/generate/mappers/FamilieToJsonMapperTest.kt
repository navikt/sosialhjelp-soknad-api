package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.FamilieToJsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator as validator

class FamilieToJsonMapperTest : AbstractMapperTest() {
    private val mapper = FamilieToJsonMapper.Mapper

    @Test
    fun `Status Gift uten ektefelle skal kaste valideringsfeil`() {
        skipAfterEach = true

        json =
            FamilieToJsonMapper.doMapping(
                Familie(
                    soknadId = UUID.randomUUID(),
                    sivilstatus = Sivilstatus.GIFT,
                ),
                json,
            )

        val soknad = requireNotNull(json.soknad)
        val familie = requireNotNull(soknad.data.familie)
        json = json.copy(soknad = soknad.copy(data = soknad.data.copy(familie = familie.copy(sivilstatus = requireNotNull(familie.sivilstatus).copy(ektefelle = null)))))

        objectMapper.writeValueAsString(json.soknad)
            .also {
                assertThatThrownBy { validator.ensureValidSoknad(it) }
                    .isInstanceOf(JsonSosialhjelpValidationException::class.java)
            }
    }

    @Test
    fun `Status gift uten ektefelle skal gi Ektefelle med tomt navn`() {
        json =
            FamilieToJsonMapper.doMapping(
                Familie(
                    soknadId = UUID.randomUUID(),
                    sivilstatus = Sivilstatus.GIFT,
                ),
                json,
            )

        with(requireNotNull(requireNotNull(requireNotNull(json.soknad).data.familie).sivilstatus)) {
            assertThat(status).isEqualTo(JsonSivilstatus.Status.valueOf(Sivilstatus.GIFT.name))
            assertThat(ektefelle).isNotNull
            assertThat(requireNotNull(ektefelle).navn).isNotNull
            assertThat(requireNotNull(requireNotNull(ektefelle).navn).fornavn).isBlank()
            assertThat(requireNotNull(requireNotNull(ektefelle).navn).mellomnavn).isBlank()
            assertThat(requireNotNull(requireNotNull(ektefelle).navn).etternavn).isBlank()
        }
    }
}
