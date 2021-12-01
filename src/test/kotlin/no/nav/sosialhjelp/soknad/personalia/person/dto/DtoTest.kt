package no.nav.sosialhjelp.soknad.personalia.person.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.client.pdl.HentPersonDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class DtoTest {

    private val pdlMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())

    @Test
    fun adressebeskyttelseJson() {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlAdressebeskyttelseResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        val pdlAdressebeskyttelse = pdlMapper.readValue<HentPersonDto<PersonAdressebeskyttelseDto>>(jsonString)
        assertThat(pdlAdressebeskyttelse).isNotNull
        assertThat(pdlAdressebeskyttelse.data.hentPerson?.adressebeskyttelse?.get(0)?.gradering).isEqualTo(Gradering.STRENGT_FORTROLIG)
    }

    @Test
    internal fun barnJson() {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlBarnResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val barnDto = pdlMapper.readValue<HentPersonDto<BarnDto>>(jsonString)

        assertThat(barnDto).isNotNull
        assertThat(barnDto.data.hentPerson?.adressebeskyttelse?.get(0)?.gradering).isEqualTo(Gradering.UGRADERT)
    }

    @Test
    internal fun ektefelleJson() {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlEktefelleResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val ektefelleDto = pdlMapper.readValue<HentPersonDto<EktefelleDto>>(jsonString)

        assertThat(ektefelleDto).isNotNull
        assertThat(ektefelleDto.data.hentPerson?.adressebeskyttelse?.get(0)?.gradering).isEqualTo(Gradering.UGRADERT)
    }

    @Test
    internal fun personJson() {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val personDto = pdlMapper.readValue<HentPersonDto<PersonDto>>(jsonString)

        assertThat(personDto).isNotNull
        assertThat(personDto.data.hentPerson?.navn?.get(0)?.fornavn).isEqualTo("TEST")
        assertThat(personDto.data.hentPerson?.navn?.get(0)?.etternavn).isEqualTo("PERSON")
    }
}
