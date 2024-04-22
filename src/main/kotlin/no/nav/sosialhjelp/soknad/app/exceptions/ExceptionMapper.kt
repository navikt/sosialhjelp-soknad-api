package no.nav.sosialhjelp.soknad.app.exceptions

import java.net.URI
import no.nav.security.token.support.core.exceptions.IssuerConfigurationException
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.MetaDataNotAvailableException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.pdf.PdfGenereringException
import no.nav.sosialhjelp.soknad.v2.NotValidInputException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DuplikatFilException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
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

@ControllerAdvice
class ExceptionMapper(
    @Value("\${loginservice.url}") private val loginserviceUrl: String,
) : ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun handleSoknadApiException(e: SosialhjelpSoknadApiException): ResponseEntity<Feilmelding> {
        val response =
            when (e) {
                is UgyldigOpplastingTypeException -> {
                    log.warn("Feilet opplasting", e)
                    ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                }
                is OpplastingException -> {
                    log.warn("Feilet opplasting", e)
                    ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                }
                is AuthorizationException -> {
                    log.warn("Ikke tilgang til ressurs", e)
                    return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Feilmelding(e.id, "Ikke tilgang til ressurs"))
                }
                is SoknadAlleredeSendtException -> {
                    log.warn("Søknad har allerede blitt sendt inn, kan ikke navigere til siden.", e)
                    return ResponseEntity
                        .status(HttpStatus.GONE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Feilmelding(e.id, "Søknad har blitt sendt inn."))
                }
                is IkkeFunnetException -> {
                    log.warn(e.message, e)
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                }
                is TjenesteUtilgjengeligException -> {
                    log.warn("REST-kall feilet: Ekstern tjeneste er utilgjengelig", e)
                    ResponseEntity.internalServerError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
                }
                is SendingTilKommuneErMidlertidigUtilgjengeligException -> {
                    log.error(e.message, e)
                    return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "innsending_midlertidig_utilgjengelig",
                                message = "Tjenesten er midlertidig utilgjengelig hos kommunen",
                            ),
                        )
                }
                is SendingTilKommuneErIkkeAktivertException -> {
                    log.error(e.message, e)
                    return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "innsending_ikke_aktivert",
                                message = "Tjenesten er ikke aktivert hos kommunen",
                            ),
                        )
                }
                is SendingTilKommuneUtilgjengeligException -> {
                    log.error(e.message, e)
                    return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "innsending_ikke_tilgjengelig",
                                message = "Tjenesten er midlertidig ikke tilgjengelig",
                            ),
                        )
                }
                is SoknadenHarNedetidException -> {
                    log.warn(e.message, e)
                    return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "nedetid",
                                message = "Søknaden har planlagt nedetid nå",
                            ),
                        )
                }
                is PdfGenereringException -> {
                    log.error(e.message, e)
                    return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "pdf_generering",
                                message = "Innsending av søknad feilet",
                            ),
                        )
                }
                is SoknadUnderArbeidIkkeFunnetException -> {
                    log.warn(e.message, e)
                    return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "soknad_not_found",
                                message = "Ingen søknad med denne behandlingsId funnet",
                            ),
                        )
                }
                is PdlApiException -> {
                    log.error("Kall til PDL feilet", e)
                    ResponseEntity.internalServerError()
                        .header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
                }
                is DuplikatFilException -> {
                    log.info("Bruker lastet opp allerede opplastet fil")
                    return ResponseEntity
                        .status(HttpStatus.NOT_ACCEPTABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "duplikat_fil",
                                message = "Fil er allerede lastet opp",
                            ),
                        )
                }
                is FileConversionException -> {
                    log.warn("Filkonverteringsfeil: ${e.message}", e)

                    return ResponseEntity
                        .status(e.httpStatus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = "filkonvertering_error",
                                message = "${e.message}",
                            ),
                        )
                }
                is NotValidInputException -> {
                    log.error("Ugyldige input: ${e.message}, id: ${e.id}")

                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            Feilmelding(
                                id = e.id,
                                message = e.message,
                            ),
                        )
                }
                else -> {
                    log.error("REST-kall feilet", e)
                    ResponseEntity.internalServerError()
                        .header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
                }
            }
        return response.contentType(MediaType.APPLICATION_JSON).body(Feilmelding(e.id, e.message))
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(e: Throwable): ResponseEntity<*> {
        return when (e) {
            is HttpStatusCodeException -> {
                when (e.statusCode) {
                    HttpStatus.UNAUTHORIZED -> {
                        log.debug(e.message, e)
                        return createUnauthorizedWithLoginLocationResponse("Autentiseringsfeil")
                    }
                    HttpStatus.FORBIDDEN -> {
                        log.debug(e.message, e)
                        return createUnauthorizedWithLoginLocationResponse("Autoriseringsfeil")
                    }
                    HttpStatus.NOT_FOUND -> {
                        log.warn(e.message, e)
                    }
                    else -> {
                        log.error(e.message, e)
                    }
                }
                ResponseEntity
                    .status(e.statusCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Feilmelding(WEB_APPLICATION_ERROR, "Noe uventet feilet"))
            }
            is SamtidigOppdateringException -> {
                log.warn(e.message, e)
                ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Feilmelding(WEB_APPLICATION_ERROR, "Samtidig oppdatering av søknad"))
            }
            else -> {
                log.error("Noe uventet feilet: ${e.message}", e)

                ResponseEntity
                    .internalServerError()
                    .header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Feilmelding(UNEXPECTED_ERROR, "Noe uventet feilet"))
            }
        }
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class, JwtTokenMissingException::class])
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    fun handleJwtTokenExceptions(e: RuntimeException): ResponseEntity<UnauthorizedMelding> {
        log.info("Bruker er ikke autentisert (enda). Sender 401 med loginurl. Feilmelding: ${e.message}")
        return createUnauthorizedWithLoginLocationResponse("Autentiseringsfeil")
    }

    @ExceptionHandler(value = [MetaDataNotAvailableException::class, IssuerConfigurationException::class])
    fun handleTokenValidationConfigurationExceptions(e: RuntimeException): ResponseEntity<Feilmelding> {
        log.error("Klarer ikke hente metadata fra discoveryurl eller problemer ved konfigurering av issuer. Feilmelding: ${e.message}")
        return ResponseEntity
            .internalServerError()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Feilmelding(UNEXPECTED_ERROR, "Noe uventet feilet"))
    }

    override fun handleMaxUploadSizeExceededException(
        ex: MaxUploadSizeExceededException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.warn("Feilet opplasting", ex)
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Feilmelding(
                    id = "vedlegg.opplasting.feil.forStor",
                    message = "Kunne ikke lagre fil fordi total filstørrelse er for stor",
                ),
            )
    }

    private fun createUnauthorizedWithLoginLocationResponse(message: String): ResponseEntity<UnauthorizedMelding> {
        val loginUrl = URI.create(loginserviceUrl)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .location(loginUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(UnauthorizedMelding(WEB_APPLICATION_ERROR, message, loginUrl))
    }

    companion object {
        private val log by logger()

        private const val WEB_APPLICATION_ERROR = "web_application_error"
        private const val UNEXPECTED_ERROR = "unexpected_error"
    }
}
