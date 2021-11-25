package no.nav.sosialhjelp.soknad.adressesok.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.client.pdl.AdressesokDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class AdressesokDtoTest {

    private val pdlMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())

    @Test
    fun deserialiseringAvAdresseSokResponseJson() {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlSokAdresseResponse.json")
        assertThat(resourceAsStream).isNotNull

        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        val response = pdlMapper.readValue<AdressesokDto>(jsonString)

        assertThat(response).isNotNull
        assertThat(response.data?.sokAdresse?.hits).hasSize(1)
        assertThat(response.data?.sokAdresse?.hits?.get(0)?.score).isZero
        assertThat(response.data?.sokAdresse?.hits?.get(0)?.vegadresse?.adressenavn).isEqualTo("Heggsnipvegen")
    }
}
