package no.nav.sosialhjelp.soknad.navenhet.bydel

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@Configuration
open class BydelConfig {

    @Bean
    open fun bydelFordelingService(): BydelFordelingService {
        return BydelFordelingService(markaBydelFordeling)
    }

    private val markaBydelFordeling: List<BydelFordeling>
        get() {
            val json = readBydelsfordelingFromFile()
            return try {
                objectMapper.readValue(json)
            } catch (e: JsonProcessingException) {
                throw SosialhjelpSoknadApiException("BydelFordeling marka: Failed to parse json", e)
            }
        }

    private fun readBydelsfordelingFromFile(): String {
        val resource = ClassPathResource("pdl/marka-bydelsfordeling.json")
        return try {
            BufferedReader(InputStreamReader(resource.inputStream, StandardCharsets.UTF_8))
                .use {
                    it.lines().collect(Collectors.joining("\n"))
                }
        } catch (e: IOException) {
            throw SosialhjelpSoknadApiException("BydelFordeling marka: Failed to read file", e)
        }
    }

    private val objectMapper = jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
}
