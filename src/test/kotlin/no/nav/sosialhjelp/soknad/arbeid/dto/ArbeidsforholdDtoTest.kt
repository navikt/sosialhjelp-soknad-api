package no.nav.sosialhjelp.soknad.arbeid.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class ArbeidsforholdDtoTest {

    @Test
    fun skalDeserialisereArbeidsforholdResponse() {
        val objectMapper = jacksonObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(JavaTimeModule())

        val resourceAsStream = ClassLoader.getSystemResourceAsStream("arbeidsforhold/aaregResponse.json")
        assertThat(resourceAsStream).isNotNull

        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        val arbeidsforholdDtoList = objectMapper.readValue<List<ArbeidsforholdDto>>(jsonString)
        val dto = arbeidsforholdDtoList[0]
        assertThat(dto.ansettelsesperiode?.periode?.fom).hasToString("2014-07-01")
        assertThat(dto.ansettelsesperiode?.periode?.tom).hasToString("2015-12-31")
        assertThat(dto.arbeidsavtaler).hasSize(1)
        assertThat(dto.arbeidsavtaler!![0].stillingsprosent).isEqualTo(49.5)
        assertThat(dto.arbeidsforholdId).isEqualTo("abc-321")
        assertThat(dto.arbeidsgiver).isExactlyInstanceOf(OrganisasjonDto::class.java)
        assertThat(dto.arbeidstaker?.offentligIdent).isEqualTo("31126700000")
    }
}
