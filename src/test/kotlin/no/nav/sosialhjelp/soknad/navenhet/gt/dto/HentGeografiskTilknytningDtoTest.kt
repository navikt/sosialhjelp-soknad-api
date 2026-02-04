package no.nav.sosialhjelp.soknad.navenhet.gt.dto

import no.nav.sosialhjelp.soknad.app.client.pdl.HentGeografiskTilknytningDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets

internal class HentGeografiskTilknytningDtoTest {
    private val pdlMapper =
        jacksonMapperBuilder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .build()

    @Test
    fun deserialiseringAvResponseJson() {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlHentGeografiskTilknytningResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        val response = pdlMapper.readValue<HentGeografiskTilknytningDto>(jsonString)
        assertThat(response).isNotNull
        assertThat(response.data.hentGeografiskTilknytning?.gtType).isEqualTo(GtType.BYDEL)
        assertThat(response.data.hentGeografiskTilknytning?.gtKommune).isNull()
        assertThat(response.data.hentGeografiskTilknytning?.gtBydel).isEqualTo("030108")
        assertThat(response.data.hentGeografiskTilknytning?.gtLand).isNull()
    }
}
