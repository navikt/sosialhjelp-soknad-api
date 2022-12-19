package no.nav.sosialhjelp.soknad.app.exceptions

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.pdf.PdfGenereringException
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggService.Companion.MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

@ControllerAdvice
class ExceptionMapper(
    @Value("\${loginservice.url}") private val loginserviceUrl: String
) : ResponseEntityExceptionHandler() {

    @ExceptionHandler
    fun handleSoknadApiException(e: SosialhjelpSoknadApiException): ResponseEntity<Feilmelding> {
        val response = when (e) {
            is UgyldigOpplastingTypeException -> {
                log.warn("Feilet opplasting", e)
                ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            }
            is OpplastingException -> {
                log.warn("Feilet opplasting", e)
                ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            }
            is SamletVedleggStorrelseForStorException -> {
                log.warn("Feilet opplasting. Valgt fil for opplasting gjør at grensen for samlet vedleggstørrelse på ${MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB}MB overskrides.", e)
                ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            }
            is AuthorizationException -> {
                log.warn("Ikke tilgang til ressurs", e)
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Feilmelding(e.id, "Ikke tilgang til ressurs"))
            }
            is IkkeFunnetException -> {
                log.warn("Fant ikke ressurs", e)
                ResponseEntity.status(HttpStatus.NOT_FOUND)
            }
            is EttersendelseSendtForSentException -> {
                log.info("REST-kall feilet: ${e.message}", e)
                ResponseEntity.internalServerError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
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
                            message = "Tjenesten er midlertidig utilgjengelig hos kommunen"
                        )
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
                            message = "Tjenesten er ikke aktivert hos kommunen"
                        )
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
                            message = "Tjenesten er midlertidig ikke tilgjengelig"
                        )
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
                            message = "Søknaden har planlagt nedetid nå"
                        )
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
                            message = "Innsending av søknad eller ettersendelse feilet"
                        )
                    )
            }
            is SoknadUnderArbeidIkkeFunnetException -> {
                log.warn(e.message, e)
                return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        Feilmelding(
                            id = "unexpected_error",
                            message = "Noe uventet feilet."
                        )
                    )
            }
            is PdlApiException -> {
                log.error("Kall til PDL feilet", e)
                ResponseEntity.internalServerError()
                    .header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
            }
            else -> {
                log.error("REST-kall feilet", e)
                ResponseEntity.internalServerError()
                    .header(Feilmelding.NO_BIGIP_5XX_REDIRECT, "true")
            }
        }
        return response.contentType(MediaType.TEXT_PLAIN).body(Feilmelding(e.id, e.message))
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(e: Throwable): ResponseEntity<*> {
        return when (e) {
            is HttpStatusCodeException -> {
                if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                    log.debug(e.message, e)
                    return createUnauthorizedWithLoginLocationResponse("Autentiseringsfeil")
                } else if (e.statusCode == HttpStatus.FORBIDDEN) {
                    log.debug(e.message, e)
                    return createUnauthorizedWithLoginLocationResponse("Autoriseringsfeil")
                } else if (e.statusCode == HttpStatus.NOT_FOUND) {
                    log.warn(e.message, e)
                } else {
                    log.error(e.message, e)
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
                    .body(Feilmelding("unexpected_error", "Noe uventet feilet"))
            }
        }
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
    }
}
