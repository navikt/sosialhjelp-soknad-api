package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.soknad.app.client.pdl.HentPersonDto
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets

class HentPersonClientMock : HentPersonClient {
    val mapper: JsonMapper =
        jacksonMapperBuilder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .build()

    override suspend fun hentPerson(
        personId: String,
    ): PersonDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlPersonResponse = mapper.readValue<HentPersonDto<PersonDto>>(jsonString)
        return pdlPersonResponse.data.hentPerson
    }

    override suspend fun hentEktefelle(ektefelleIdent: String): EktefelleDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlEktefelleResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlEktefelleResponse = mapper.readValue<HentPersonDto<EktefelleDto>>(jsonString)
        return pdlEktefelleResponse.data.hentPerson
    }

    override suspend fun hentBarn(barnIdent: String): BarnDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlBarnResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlBarnResponse = mapper.readValue<HentPersonDto<BarnDto>>(jsonString)
        return pdlBarnResponse.data.hentPerson
    }

    override suspend fun hentAdressebeskyttelse(): PersonAdressebeskyttelseDto? {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlAdressebeskyttelseTomResponse.json")
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)

        val pdlAdressebeskyttelseResponse = mapper.readValue<HentPersonDto<PersonAdressebeskyttelseDto>>(jsonString)
        return pdlAdressebeskyttelseResponse.data.hentPerson
    }
}
