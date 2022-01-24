package no.nav.sosialhjelp.soknad.common.rest.feil

import no.nav.sosialhjelp.soknad.common.exceptions.SamtidigOppdateringException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ThrowableMapperTest {

    private val mapper = ThrowableMapper("loginserviceUrl")

    @Test
    fun skalGi409ConflictVedSamtidigOppdateringException() {
        val response = mapper.toResponse(SamtidigOppdateringException("Mulig versjonskonflikt..."))
        assertThat(response.status).isEqualTo(409)
    }
}
