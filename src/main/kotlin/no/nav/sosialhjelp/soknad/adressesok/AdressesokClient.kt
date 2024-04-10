package no.nav.sosialhjelp.soknad.adressesok

import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.graphql.client.FieldAccessException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AdressesokClient(
    private val pdlGraphQlClientBuilder: PdlGraphQlClientBuilder
) {
    fun getAdressesokResult(variables: Map<String, Any>): Mono<AdressesokResultDto> =
        pdlGraphQlClientBuilder
            .buildAzureAdClient()
            .documentName("pdl-adressesok")
            .variables(variables)
            .retrieve("sokAdresse")
            .toEntity(AdressesokResultDto::class.java)
            .doOnError {
                if (it is FieldAccessException) {
                    log.error("graphql-feil fra server: ${it.message}")
                } else {
                    log.error("nettverks/http-feil: ${it.message}")
                }
            }

    companion object {
        private val log = getLogger(AdressesokClient::class.java)
    }
}
