package no.nav.sosialhjelp.soknad.adressesok.dto

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.client.pdl.AdressesokDto
import no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class AdressesokDtoTest {

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