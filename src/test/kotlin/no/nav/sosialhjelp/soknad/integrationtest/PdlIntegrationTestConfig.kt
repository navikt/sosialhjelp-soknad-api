package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.soknad.adressesok.PdlGraphQlMockClientBuilder
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClientImpl
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.util.mockWebClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono

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
    override fun hentPerson(ident: String): Mono<PersonDto> =
        HentPersonClientImpl(PdlGraphQlMockClientBuilder(mockWebClient("pdl/pdlPersonResponse.json"))).hentPerson(ident)

    override fun hentEktefelle(ident: String): Mono<EktefelleDto> =
        HentPersonClientImpl(PdlGraphQlMockClientBuilder(mockWebClient("pdl/pdlEktefelleResponse.json"))).hentEktefelle(ident)

    override fun hentBarn(ident: String): Mono<BarnDto> =
        HentPersonClientImpl(PdlGraphQlMockClientBuilder(mockWebClient("pdl/pdlBarnResponse.json"))).hentBarn(ident)

    override fun hentAdressebeskyttelse(ident: String): Mono<PersonAdressebeskyttelseDto> =
        HentPersonClientImpl(PdlGraphQlMockClientBuilder(mockWebClient("pdl/pdlAdressebeskyttelseTomResponse.json"))).hentAdressebeskyttelse(ident)
}
