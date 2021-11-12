package no.nav.sosialhjelp.soknad.client.kodeverk.dto

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KodeverkDtoTest {

    @Test
    internal fun `skal deserialisere response`() {
        val json = """{"betydninger":{ "NOR": [ {"gyldigFra": "1900-01-01","gyldigTil": "9999-12-31","beskrivelser": {"nb": {"term": "NORGE","tekst": "NORGE" } } }] } } """

        val response = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .readValue<KodeverkDto>(json)

        assertThat(response).isNotNull
        assertThat(response.betydninger!!["NOR"]!![0].beskrivelser!!["nb"]!!.term).isEqualTo("NORGE")
    }
}
