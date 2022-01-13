package no.nav.sosialhjelp.soknad.common.rest.feil

import no.nav.sosialhjelp.soknad.business.exceptions.SamtidigOppdateringException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ThrowableMapperTest {

    private val mapper = ThrowableMapper()

    @Test
    fun skalGi409ConflictVedSamtidigOppdateringException() {
        val response = mapper.toResponse(SamtidigOppdateringException("Mulig versjonskonflikt..."))
        assertThat(response.status).isEqualTo(409)
    }
}
