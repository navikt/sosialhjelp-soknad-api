package no.nav.sosialhjelp.soknad.client.dkif.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DigitalKontaktinfoBolkTest {

    @Test
    fun skalDeserialisereResponse() {
        val json = """{"kontaktinfo": {"ident": {"personident": "ident", "kanVarsles": false, "reservert": false, "epostadresse": "noreply@nav.no", "mobiltelefonnummer": "11111111"} }, "feil": null }""".trimIndent()
        val response = ObjectMapper()
            .registerKotlinModule()
            .readValue<DigitalKontaktinfoBolk>(json)
        assertThat(response.kontaktinfo?.get("ident")!!.mobiltelefonnummer).isEqualTo("11111111")
    }
}
