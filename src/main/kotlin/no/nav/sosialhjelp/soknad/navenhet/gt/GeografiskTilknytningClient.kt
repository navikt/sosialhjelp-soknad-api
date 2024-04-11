package no.nav.sosialhjelp.soknad.navenhet.gt

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import no.nav.sosialhjelp.soknad.adressesok.PdlGraphQlClientBuilder
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.graphql.client.FieldAccessException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class GeografiskTilknytningClient(
    val pdlGraphQlClientBuilder: PdlGraphQlClientBuilder
) {
    @CircuitBreaker(name = "pdl")
    fun hentGeografiskTilknytning(ident: String): Mono<GeografiskTilknytningDto> =
        pdlGraphQlClientBuilder.buildTokenXClient(ident)
            .documentName("pdl-geografisktilknytning-query")
            .variable("ident", ident)
            .retrieve("hentGeografiskTilknytning")
            .toEntity(GeografiskTilknytningDto::class.java)
            .doOnError {
                if (it is FieldAccessException) {
                    log.error("graphql-feil fra server: ${it.message}")
                } else {
                    log.error("nettverks/http-feil: ${it.message}")
                }
            }

    companion object {
        private val log = getLogger(GeografiskTilknytningClient::class.java)
    }
}
