package no.nav.sosialhjelp.soknad.app.client.pdl

import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue

class BaseResponseTest {
    @Test
    fun `errors fra pdl deserialiseres og kaster PdlApiException`() {
        val json =
            """
            {
              "data": { "hentPerson": null },
              "errors": [ { "message": "Fant ikke person", "extensions": { "code": "not_found" } } ]
            }
            """.trimIndent()

        val response = soknadJacksonMapper.readValue<HentPersonDto<Any>>(json)

        assertThat(response.errors).hasSize(1)
        assertThatThrownBy { response.checkForPdlApiErrors() }
            .isInstanceOf(PdlApiException::class.java)
            .hasMessageContaining("Fant ikke person")
            .hasMessageContaining("not_found")
    }

    @Test
    fun `respons uten errors kaster ikke`() {
        val response = soknadJacksonMapper.readValue<HentPersonDto<Any>>("""{ "data": { "hentPerson": null } }""")

        response.checkForPdlApiErrors()
        assertThat(response.errors).isNull()
    }
}
