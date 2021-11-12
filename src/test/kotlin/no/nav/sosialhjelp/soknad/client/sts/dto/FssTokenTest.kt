package no.nav.sosialhjelp.soknad.client.sts.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class FssTokenTest {

    @Test
    fun skalDeserialisereFssToken() {
        val json = """{"access_token":"asd","token_type":"fgh","expires_in":1234}"""
        val fssToken = jacksonObjectMapper().readValue<FssToken>(json)
        Assertions.assertThat(fssToken.access_token).isEqualTo("asd")
        Assertions.assertThat(fssToken.token_type).isEqualTo("fgh")
        Assertions.assertThat(fssToken.expires_in).isEqualTo(1234L)
    }
}
