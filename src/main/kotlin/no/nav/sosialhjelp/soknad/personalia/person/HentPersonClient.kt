package no.nav.sosialhjelp.soknad.personalia.person

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import no.nav.sosialhjelp.soknad.adressesok.PdlGraphQlClientBuilder
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.graphql.client.FieldAccessException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface HentPersonClient {
    fun hentPerson(ident: String): Mono<PersonDto>
    fun hentEktefelle(ident: String): Mono<EktefelleDto>
    fun hentBarn(ident: String): Mono<BarnDto>
    fun hentAdressebeskyttelse(ident: String): Mono<PersonAdressebeskyttelseDto>
}

@Component
class HentPersonClientImpl(
    private val pdlGraphQlClientBuilder: PdlGraphQlClientBuilder
) : HentPersonClient {
    fun handleError(e: Throwable) {
        if (e is FieldAccessException) {
            log.error("graphql-feil fra server: ${e.message}")
        } else {
            log.error("nettverks/http-feil: ${e.message}")
        }
    }

    @CircuitBreaker(name = "pdl")
    override fun hentPerson(ident: String): Mono<PersonDto> = pdlGraphQlClientBuilder.buildAzureAdClient()
        .documentName("pdl-person-query")
        .variable("ident", ident)
        .retrieve("hentPerson")
        .toEntity(PersonDto::class.java)
        .doOnError(::handleError)

    @CircuitBreaker(name = "pdl")
    override fun hentEktefelle(ident: String): Mono<EktefelleDto> = pdlGraphQlClientBuilder.buildTokenXClient(ident)
        .documentName("pdl-ektefelle-query")
        .variable("ident", ident)
        .retrieve("hentPerson")
        .toEntity(EktefelleDto::class.java)
        .doOnError(::handleError)

    @CircuitBreaker(name = "pdl")
    override fun hentBarn(ident: String): Mono<BarnDto> = pdlGraphQlClientBuilder.buildAzureAdClient()
        .documentName("pdl-barn-query")
        .variable("ident", ident)
        .retrieve("hentPerson")
        .toEntity(BarnDto::class.java)
        .doOnError(::handleError)

    @CircuitBreaker(name = "pdl")
    override fun hentAdressebeskyttelse(ident: String): Mono<PersonAdressebeskyttelseDto> = pdlGraphQlClientBuilder.buildTokenXClient(ident)
        .documentName("pdl-person-adressebeskyttelse-query")
        .variable("ident", ident)
        .retrieve("hentPerson")
        .toEntity(PersonAdressebeskyttelseDto::class.java)
        .doOnError(::handleError)

    companion object {
        private val log = getLogger(HentPersonClient::class.java)
    }
}
