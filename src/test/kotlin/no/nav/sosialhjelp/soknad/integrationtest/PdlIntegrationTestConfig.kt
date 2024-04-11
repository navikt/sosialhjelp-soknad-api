package no.nav.sosialhjelp.soknad.integrationtest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sosialhjelp.soknad.app.client.pdl.HentPersonDto
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.nio.charset.StandardCharsets

@Configuration
class PdlIntegrationTestConfig {
    /**
     * overskriver pdlHentPersonConsumer for itester
     */
    @Primary
    @Bean
    fun hentPersonClient(): HentPersonClient {
        return HentPersonClientMock()
    }
}

class HentPersonClientMock : HentPersonClient {
    val mapper: ObjectMapper =
        jacksonObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    override fun hentPerson(ident: String): PersonDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlPersonResponse = mapper.readValue<HentPersonDto<PersonDto>>(jsonString)
        return pdlPersonResponse.data.hentPerson
    }

    override fun hentEktefelle(ident: String): EktefelleDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlEktefelleResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlEktefelleResponse = mapper.readValue<HentPersonDto<EktefelleDto>>(jsonString)
        return pdlEktefelleResponse.data.hentPerson
    }

    override fun hentBarn(ident: String): BarnDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlBarnResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlBarnResponse = mapper.readValue<HentPersonDto<BarnDto>>(jsonString)
        return pdlBarnResponse.data.hentPerson
    }

    override fun hentAdressebeskyttelse(ident: String): PersonAdressebeskyttelseDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlAdressebeskyttelseTomResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlAdressebeskyttelseResponse = mapper.readValue<HentPersonDto<PersonAdressebeskyttelseDto>>(jsonString)
        return pdlAdressebeskyttelseResponse.data.hentPerson
    }
}
