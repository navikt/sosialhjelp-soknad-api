package no.nav.sosialhjelp.soknad.adressesok.dto

import no.nav.sosialhjelp.soknad.app.client.pdl.AdressesokDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets

internal class AdressesokDtoTest {
    private val pdlMapper: ObjectMapper =
        jacksonMapperBuilder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .build()

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
