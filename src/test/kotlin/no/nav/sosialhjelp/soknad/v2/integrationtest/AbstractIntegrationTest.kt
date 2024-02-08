package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.app.exceptions.Feilmelding
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.BodyInserters
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    protected fun<T> doGet(uri: String, responseBodyClass: Class<T>): T {
        return webTestClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult().responseBody!!
    }

    protected fun<T> doPut(uri: String, requestBody: Any, responseBodyClass: Class<T>): T {
        return webTestClient.put()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult()
            .responseBody!!
    }

    protected fun doPutExpectError(uri: String, requestBody: Any, httpStatus: HttpStatusCode): Feilmelding {
        return webTestClient.put()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(httpStatus)
            .expectBody(Feilmelding::class.java)
            .returnResult()
            .responseBody!!
    }

    protected fun<T> doDelete(uri: String) {
        webTestClient
            .delete()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
//            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + token.serialize())
            .exchange()
            .expectStatus().isNoContent
    }
}
