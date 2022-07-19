package no.nav.sosialhjelp.soknad.navenhet.gt.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.app.client.pdl.HentGeografiskTilknytningDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class HentGeografiskTilknytningDtoTest {

    private val pdlMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())

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
