package no.nav.sosialhjelp.soknad.util

import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.File

/**
 * Mock WebClient returning response from file, with HTTP 200 and Content-Type application/json
 *
 * @param responseFilePath Path to data file, relative to resources root
 */
fun mockWebClient(responseFilePath: String): WebClient = WebClient.builder().exchangeFunction {
    Mono.just(
        ClientResponse.create(HttpStatus.OK).apply {
            header("Content-Type", "application/json")
            body(File(ClassPathResource(responseFilePath).uri).readText())
        }.build()
    )
}.build()
