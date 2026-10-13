package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.netty.handler.timeout.ReadTimeoutException
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.Exceptions
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.HttpServerResponse
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class SendSoknadClientRetryTest {
    private val soknadId = UUID.randomUUID()
    private val antallKall = AtomicInteger()
    private var svar: (Int, HttpServerResponse) -> Mono<Void> = { _, _ -> Mono.never() }
    private lateinit var server: DisposableServer

    @BeforeEach
    fun setup() {
        StaticSubjectHandlerImpl()
            .apply { setUser("12345678910") }
            .also { SubjectHandlerUtils.setNewSubjectHandlerImpl(it) }

        server =
            HttpServer.create()
                .port(0)
                .route { routes ->
                    routes.post("/digisos/api/v2/soknader/{kommunenummer}/{soknadId}") { req, res ->
                        val kallNr = antallKall.incrementAndGet()
                        req.receive().then().then(Mono.defer { svar(kallNr, res) })
                    }
                }
                .bindNow()
    }

    @AfterEach
    fun tearDown() {
        server.disposeNow()
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun `ReadTimeout og 400 med allerede mottatt paa retry skal gi FiksError`() {
        val digisosId = UUID.randomUUID()
        svar = { kallNr, res ->
            if (kallNr == 1) Mono.never() else res.sendError(alleredeMottatt(digisosId))
        }

        val response = createClient().lastOppFiler()

        assertThat(antallKall.get()).isEqualTo(2)
        assertThat(response).isInstanceOf(SendSoknadResponse.FiksError::class.java)
        (response as SendSoknadResponse.FiksError).errorMessage.message!!
            .also { assertThat(FiksUtils.isAlleredeMottatt(it, soknadId)).isEqualTo(digisosId) }
    }

    @Test
    fun `ReadTimeout og 400 med mottak pabegynt paa retry skal gi FiksError`() {
        svar = { kallNr, res ->
            if (kallNr == 1) Mono.never() else res.sendError(mottakPabegynt())
        }

        val response = createClient().lastOppFiler()

        assertThat(antallKall.get()).isEqualTo(2)
        assertThat(response).isInstanceOf(SendSoknadResponse.FiksError::class.java)
        (response as SendSoknadResponse.FiksError).errorMessage.message!!
            .also { assertThat(FiksUtils.isMottakPabegynt(it, soknadId)).isTrue() }
    }

    @Test
    fun `ReadTimeout og vellykket retry skal gi Success`() {
        val digisosId = UUID.randomUUID()
        svar = { kallNr, res ->
            if (kallNr == 1) Mono.never() else res.sendString(Mono.just("\"$digisosId\"")).then()
        }

        val response = createClient().lastOppFiler()

        assertThat(antallKall.get()).isEqualTo(2)
        assertThat(response).isInstanceOf(SendSoknadResponse.Success::class.java)
        assertThat((response as SendSoknadResponse.Success).digisosId).isEqualTo(digisosId)
    }

    @Test
    fun `ReadTimeout paa retry skal gi Error med RetryExhausted og ReadTimeoutException som cause`() {
        svar = { _, _ -> Mono.never() }

        val response = createClient().lastOppFiler()

        assertThat(antallKall.get()).isEqualTo(2)
        assertThat(response).isInstanceOf(SendSoknadResponse.Error::class.java)
        val e = (response as SendSoknadResponse.Error).e
        assertThat(Exceptions.isRetryExhausted(e)).isTrue()
        assertThat(e.cause).isInstanceOf(WebClientRequestException::class.java)
        assertThat(e.cause?.cause).isInstanceOf(ReadTimeoutException::class.java)
    }

    @Test
    fun `400 uten ReadTimeout skal ikke gi retry`() {
        svar = { _, res -> res.sendError(mottakPabegynt()) }

        val response = createClient().lastOppFiler()

        assertThat(antallKall.get()).isEqualTo(1)
        assertThat(response).isInstanceOf(SendSoknadResponse.FiksError::class.java)
    }

    private fun createClient() =
        SendSoknadClient(
            FiksUtils.createFiksWebClient(
                webClientBuilder = WebClient.builder().baseUrl("http://localhost:${server.port()}"),
                integrasjonsidFiks = "integrasjonsid",
                integrasjonpassordFiks = "integrasjonpassord",
                readTimeout = Duration.ofMillis(500),
            ),
        )

    private fun SendSoknadClient.lastOppFiler() =
        lastOppFiler(
            soknadJson = "{}",
            tilleggsinformasjonJson = "{}",
            vedleggJson = "{}",
            filer = emptyList(),
            kommunenummer = "5555",
            soknadId = soknadId,
        )

    private fun HttpServerResponse.sendError(errorMessage: ErrorMessage): Mono<Void> =
        status(HttpStatus.BAD_REQUEST.value())
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .sendString(Mono.just(jacksonObjectMapper().writeValueAsString(errorMessage)))
            .then()

    private fun alleredeMottatt(digisosId: UUID) =
        createErrorMessage(
            "Soknad med tilhørende navEksternRefId $soknadId finnes allerede i Fiks-Digisos med DigisosId $digisosId",
        )

    private fun mottakPabegynt() = createErrorMessage("Mottak av søknad med navExternRefId $soknadId er allerede påbegynt")

    private fun createErrorMessage(message: String) =
        ErrorMessage(
            timestamp = System.currentTimeMillis(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            errorId = UUID.randomUUID().toString(),
            path = "/digisos/api/v2/soknader/5555/$soknadId",
            message = message,
            errorCode = null,
            errorJson = null,
            originalPath = null,
        )
}
