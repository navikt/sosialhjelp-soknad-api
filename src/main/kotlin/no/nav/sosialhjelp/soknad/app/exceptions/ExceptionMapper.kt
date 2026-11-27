package no.nav.sosialhjelp.soknad.app.exceptions

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.pdf.PdfGenereringException
import no.nav.sosialhjelp.soknad.v2.AntallSoknaderSendtException
import no.nav.sosialhjelp.soknad.v2.bostotte.UpdateBostotteException
import no.nav.sosialhjelp.soknad.v2.kontakt.service.UgyldigTelefonnummerException
import no.nav.sosialhjelp.soknad.v2.lifecycle.MottakPabegyntException
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiElementFinnesIkkeException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ControllerAdvice
class ExceptionMapper(
    @param:Value("\${loginservice.url:null}") private val loginserviceUrl: URI?,
) : ResponseEntityExceptionHandler() {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.error("Returning HTTP 400 Bad Request. message: ${ex.message}, cause: ${ex.cause}", ex)
        return buildError(HttpStatus.BAD_REQUEST, SoknadApiError(SoknadApiErrorType.UgyldigInput))
    }

    @ExceptionHandler(SosialhjelpSoknadApiException::class)
    fun handleSoknadApiException(e: SosialhjelpSoknadApiException): ResponseEntity<SoknadApiError> =
        when (e) {
            is TjenesteUtilgjengeligException -> {
                log.warn("REST-kall feilet: Ekstern tjeneste er utilgjengelig", e)
                buildError(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    SoknadApiError(SoknadApiErrorType.InnsendingUtilgjengelig, e),
                )
            }
            is SendingTilKommuneErMidlertidigUtilgjengeligException -> {
                log.error(e.message, e)
                buildError(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    SoknadApiError(SoknadApiErrorType.InnsendingMidlertidigUtilgjengelig),
                )
            }
            is SendingTilKommuneErIkkeAktivertException -> {
                log.error(e.message, e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.InnsendingIkkeAktivert))
            }
            is SendingTilKommuneUtilgjengeligException -> {
                log.error(e.message, e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.InnsendingUtilgjengelig))
            }
            is SoknadenHarNedetidException -> {
                log.warn(e.message, e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.PlanlagtNedetid))
            }
            is PdfGenereringException -> {
                log.error(e.message, e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.PdfGenereringFeilet))
            }
            is PdlApiException -> {
                log.error("Kall til PDL feilet", e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.PdlKallFeilet))
            }
            is OkonomiElementFinnesIkkeException -> {
                log.error("Feil ved oppdatering av okonomi-element: ${e.message}", e)
                buildError(HttpStatus.NOT_FOUND, SoknadApiError(SoknadApiErrorType.NotFound, e))
            }
            is UpdateBostotteException -> {
                log.error("Feil ved oppdatering av Bostotte", e)
                buildError(HttpStatus.BAD_REQUEST, SoknadApiError(SoknadApiErrorType.UgyldigInput, e))
            }
            is AntallSoknaderSendtException -> {
                log.error(e.message, e)
                buildError(HttpStatus.TOO_MANY_REQUESTS, SoknadApiError(SoknadApiErrorType.AntallSoknaderSendt, e))
            }
            is UgyldigTelefonnummerException -> {
                log.error(e.message, e)
                buildError(HttpStatus.BAD_REQUEST, SoknadApiError(SoknadApiErrorType.UgyldigInput, e))
            }
            is BrokenSoknadException -> {
                log.error("Innsending feiler fordi søknaden er i en ugyldig tilstand", e)
                buildError(HttpStatus.BAD_REQUEST, SoknadApiError(SoknadApiErrorType.BrokenSoknad))
            }
            is MottakPabegyntException -> {
                log.error("Innsending feiler fordi mottak allerede er påbegynt hos FIKS", e)
                buildError(HttpStatus.BAD_REQUEST, SoknadApiError(SoknadApiErrorType.MottakPabegynt, e))
            }
            else -> {
                log.error("REST-kall feilet", e)
                buildError(HttpStatus.INTERNAL_SERVER_ERROR, SoknadApiError(SoknadApiErrorType.GeneralError, e))
            }
        }

    @ExceptionHandler(value = [AuthorizationException::class])
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<SoknadApiError> {
        log.info("Ikke tilgang", e)
        return buildError(
            HttpStatus.FORBIDDEN,
            SoknadApiError(error = e.type),
        )
    }

    @ExceptionHandler(value = [HttpStatusCodeException::class])
    fun handleHttpClientException(e: HttpStatusCodeException): ResponseEntity<*> {
        when (e.statusCode) {
            HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN -> log.debug(e.message, e)
            HttpStatus.NOT_FOUND -> log.warn(e.message, e)
            else -> log.error(e.message, e)
        }

        return if (listOf(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN).contains(e.statusCode)) {
            loginserviceUrl?.let { buildError(HttpStatus.UNAUTHORIZED, UnauthorizedMelding(it)) } ?: buildError(HttpStatus.UNAUTHORIZED, "Unauthorized")
        } else {
            buildError(e.statusCode, SoknadApiError(SoknadApiErrorType.GeneralError))
        }
    }

    // Sørger for at alle andre unntak som ikke er håndtert ikke lekker ut til klienten, men logges og returnerer 500
    @ExceptionHandler(Throwable::class)
    fun handleThrowable(e: Throwable): ResponseEntity<*> {
        log.error("Noe uventet feilet: ${e.message}", e)
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, SoknadApiError(SoknadApiErrorType.GeneralError))
    }

    @ExceptionHandler(value = [SamtidigOppdateringException::class])
    @ResponseStatus(value = HttpStatus.CONFLICT)
    fun handleConflictExceptions(e: RuntimeException): ResponseEntity<SoknadApiError> {
        log.warn(e.message, e)
        return buildError(HttpStatus.CONFLICT, SoknadApiError(SoknadApiErrorType.SoknadUpdateConflict))
    }

    @ExceptionHandler(value = [IkkeFunnetException::class, SoknadFinnesIkkeException::class])
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleNotFoundExceptions(e: RuntimeException): ResponseEntity<SoknadApiError> {
        when (e) {
            is SoknadFinnesIkkeException -> log.info("Finnes ingen søknad med id: ${e.soknadId}")
            else -> log.warn("Fant ikke: ${e.message}", e)
        }
        return buildError(
            HttpStatus.NOT_FOUND,
            SoknadApiError(
                error = SoknadApiErrorType.NotFound,
                e = SosialhjelpSoknadApiException(message = e.message, cause = e),
            ),
        )
    }

    @ExceptionHandler(InnsendingFeiletException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInnsendingFeiletException(e: InnsendingFeiletException): ResponseEntity<InnsendingFeiletError> {
        log.error("Innsending feilet", e)
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, InnsendingFeiletError(e.deletionDate.toDateString()))
    }

    @ExceptionHandler(SoknadLifecycleException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleSoknadLifecycleException(e: SoknadLifecycleException): ResponseEntity<SoknadApiError> {
        log.error("Ukjent feil for søknad", e)
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, SoknadApiError(SoknadApiErrorType.GeneralError))
    }

    companion object {
        private val log by logger()
    }
}

private fun <T : Any> buildError(
    status: HttpStatusCode,
    body: T? = null,
): ResponseEntity<T> =
    ResponseEntity
        .status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)

private fun LocalDateTime.toDateString(): String =
    toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
