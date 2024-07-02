package no.nav.sosialhjelp.soknad.app.exceptions

import no.nav.security.token.support.core.exceptions.IssuerConfigurationException
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.MetaDataNotAvailableException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.pdf.PdfGenereringException
import no.nav.sosialhjelp.soknad.v2.NotValidInputException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadDuplicateFilename
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadError
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadFileEncrypted
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadPossibleVirus
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadUnsupportedMediaType
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FileConversionException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

@ControllerAdvice
class ExceptionMapper(
    @Value("\${loginservice.url}") private val loginserviceUrl: URI,
) : ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun handleSoknadApiException(e: SosialhjelpSoknadApiException): ResponseEntity<SoknadApiError> =
        when (e) {
            is SoknadAlleredeSendtException -> {
                log.warn("Søknad har allerede blitt sendt inn", e)
                buildError(HttpStatus.GONE, SoknadApiError(SoknadApiErrorType.SoknadAlleredeSendt))
            }

            is TjenesteUtilgjengeligException -> {
                log.warn("REST-kall feilet: Ekstern tjeneste er utilgjengelig", e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.InnsendingUtilgjengelig, e))
            }

            is SendingTilKommuneErMidlertidigUtilgjengeligException -> {
                log.error(e.message, e)
                buildError(HttpStatus.SERVICE_UNAVAILABLE, SoknadApiError(SoknadApiErrorType.InnsendingMidlertidigUtilgjengelig))
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

            is DokumentUploadDuplicateFilename -> {
                log.info("Bruker lastet opp allerede opplastet fil")
                buildError(HttpStatus.NOT_ACCEPTABLE, SoknadApiError(SoknadApiErrorType.DokumentUploadDuplicateFilename))
            }

            is DokumentUploadUnsupportedMediaType -> {
                log.warn("UgyldigOpplastingTypeException", e)
                buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, SoknadApiError(SoknadApiErrorType.DokumentUploadUnsupportedMediaType, e))
            }

            is DokumentUploadFileEncrypted -> {
                log.warn("DokumentUploadFileEncrypted", e)
                buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, SoknadApiError(SoknadApiErrorType.DokumentUploadFileEncrypted, e))
            }

            is FileConversionException -> {
                log.warn("Filkonverteringsfeil: ${e.message}", e)
                buildError(e.httpStatus, SoknadApiError(SoknadApiErrorType.DokumentKonverteringFeilet, e))
            }

            else -> {
                log.error("REST-kall feilet", e)
                buildError(HttpStatus.INTERNAL_SERVER_ERROR, SoknadApiError(SoknadApiErrorType.GeneralError, e))
            }
        }

    @ExceptionHandler(value = [DokumentUploadError::class])
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    fun handleDokumentUploadError(e: DokumentUploadError): ResponseEntity<SoknadApiError> {
        log.warn("Feil ved opplasting av dokument", e)
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, SoknadApiError(SoknadApiErrorType.DokumentUploadError))
    }

    @ExceptionHandler(value = [AuthorizationException::class])
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<SoknadApiError> {
        log.warn("Ikke tilgang til ressurs", e)
        return buildError(HttpStatus.FORBIDDEN, SoknadApiError(SoknadApiErrorType.Forbidden))
    }

    @ExceptionHandler(value = [HttpStatusCodeException::class])
    fun handleHttpClientException(e: HttpStatusCodeException): ResponseEntity<*> {
        when (e.statusCode) {
            HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN -> log.debug(e.message, e)
            HttpStatus.NOT_FOUND -> log.warn(e.message, e)
            else -> log.error(e.message, e)
        }

        return if (listOf(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN).contains(e.statusCode)) {
            buildError(HttpStatus.UNAUTHORIZED, UnauthorizedMelding(loginserviceUrl))
        } else {
            buildError(e.statusCode, SoknadApiError(SoknadApiErrorType.GeneralError))
        }
    }

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

    @ExceptionHandler(value = [IkkeFunnetException::class, SoknadUnderArbeidIkkeFunnetException::class])
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleNotFoundExceptions(e: RuntimeException): ResponseEntity<SoknadApiError> {
        log.warn("Fant ikke:", e)
        return buildError(HttpStatus.NOT_FOUND, SoknadApiError(SoknadApiErrorType.NotFound))
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class, JwtTokenMissingException::class])
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    fun handleJwtTokenExceptions(e: RuntimeException): ResponseEntity<UnauthorizedMelding> {
        log.info("Bruker er ikke autentisert (enda). Sender 401 med loginurl. Feilmelding: ${e.message}")
        return buildError(HttpStatus.UNAUTHORIZED, UnauthorizedMelding(loginserviceUrl))
    }

    @ExceptionHandler(value = [NotValidInputException::class])
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleNotValidInputException(e: NotValidInputException): ResponseEntity<SoknadApiError> {
        log.error("Ugyldige input: ${e.message}, id: ${e.id}")
        return buildError(HttpStatus.BAD_REQUEST, SoknadApiError(SoknadApiErrorType.UgyldigInput, e))
    }

    @ExceptionHandler(value = [MetaDataNotAvailableException::class, IssuerConfigurationException::class])
    fun handleTokenValidationConfigurationExceptions(e: RuntimeException): ResponseEntity<SoknadApiError> {
        log.error("Klarer ikke hente metadata fra discoveryurl eller problemer ved konfigurering av issuer. Feilmelding: ${e.message}")
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, SoknadApiError(SoknadApiErrorType.GeneralError))
    }

    @ExceptionHandler(value = [DokumentUploadPossibleVirus::class])
    fun handleDokumentUploadPossibleVirus(e: DokumentUploadPossibleVirus): ResponseEntity<SoknadApiError> {
        log.warn("Mulig virusfunn ved opplasting", e)
        return buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, SoknadApiError(SoknadApiErrorType.DokumentUploadPossibleVirus))
    }

    override fun handleMaxUploadSizeExceededException(
        ex: MaxUploadSizeExceededException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.warn("Feilet opplasting", ex)
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, SoknadApiError(SoknadApiErrorType.DokumentUploadTooLarge))
    }

    companion object {
        private val log by logger()

        private fun <T> buildError(
            status: HttpStatusCode,
            body: T,
        ): ResponseEntity<T> =
            ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
    }
}
