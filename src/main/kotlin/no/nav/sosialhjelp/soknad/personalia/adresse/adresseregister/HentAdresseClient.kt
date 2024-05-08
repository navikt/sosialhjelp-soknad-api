package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import no.nav.sosialhjelp.soknad.adressesok.PdlGraphQlClientBuilder
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.AdresseDto
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto
import org.springframework.graphql.client.FieldAccessException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HentAdresseClient(
    private val pdlGraphQlClientBuilder: PdlGraphQlClientBuilder
) {
    fun hentMatrikkelAdresse(matrikkelId: String): Mono<MatrikkeladresseDto> =
        pdlGraphQlClientBuilder
            .buildAzureAdClient()
            .documentName("pdl-hentadresse")
            .variable("matrikkelId", matrikkelId)
            .retrieve("hentAdresse")
            .toEntity(AdresseDto::class.java)
            .doOnError {
                if (it is FieldAccessException) {
                    log.error("graphql-feil fra server: ${it.message}")
                } else {
                    log.error("nettverks/http-feil: ${it.message}")
                }
            }.map { it.matrikkeladresse ?: throw PdlApiException("Fant ikke matrikkeladresse") }

    companion object {
        private val log by logger()
    }
}
