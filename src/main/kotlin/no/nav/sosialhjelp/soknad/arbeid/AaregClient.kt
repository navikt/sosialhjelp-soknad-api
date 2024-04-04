package no.nav.sosialhjelp.soknad.arbeid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class AaregClient(
    @Value("\${aareg_url}") private val aaregUrl: String,
    @Value("\${aareg_audience}") private val aaregAudience: String,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder
) {
    private val arbeidsforholdMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(JavaTimeModule())

    private val sokeperiode: Sokeperiode get() = Sokeperiode(LocalDate.now().minusMonths(3), LocalDate.now())

    private val tokenxToken: String get() = runBlocking {
        tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), aaregAudience)
    }

    private val webClient = unproxiedWebClientBuilder(webClientBuilder)
        .baseUrl(aaregUrl)
        .codecs {
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(arbeidsforholdMapper))
        }
        .build()

    private val queryParamsPart = "?sporingsinformasjon={sporingsinformasjon}&regelverk={regelverk}&ansettelsesperiodeFom={fom}&ansettelsesperiodeTom={tom}"

    fun finnArbeidsforholdForArbeidstaker(fodselsnummer: String): List<ArbeidsforholdDto>? {
        try {
            return webClient.get()
                .uri("/v1/arbeidstaker/arbeidsforhold$queryParamsPart", false, A_ORDNINGEN, sokeperiode.fom, sokeperiode.tom)
                .header(AUTHORIZATION, BEARER + tokenxToken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_NAV_PERSONIDENT, fodselsnummer)
                .retrieve()
                .bodyToMono<List<ArbeidsforholdDto>>()
                .block()
        } catch (e: WebClientResponseException.BadRequest) {
            log.warn("Aareg.api - 400 Bad Request - Ugyldig(e) parameter(e) i request", e)
            return null
        } catch (e: WebClientResponseException.Unauthorized) {
            log.warn("Aareg.api - 401 Unauthorized - Token mangler eller er ugyldig", e)
            return null
        } catch (e: WebClientResponseException.Forbidden) {
            log.warn("Aareg.api - 403 Forbidden - Ingen tilgang til forespurt ressurs", e)
            return null
        } catch (e: WebClientResponseException.NotFound) {
            log.warn("Aareg.api - 404 Not Found - Fant ikke arbeidsforhold for bruker", e)
            return null
        } catch (e: WebClientResponseException.ServiceUnavailable) {
            log.error("Aareg.api - ${e.statusCode} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: WebClientResponseException.InternalServerError) {
            log.error("Aareg.api - ${e.statusCode} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: Exception) {
            log.error("Aareg.api - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        }
    }

    fun ping() {
        webClient.options()
            .uri("/ping")
            .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
            .retrieve()
            .bodyToMono<Any>()
            .block()
    }

    companion object {
        private val log by logger()
        private const val A_ORDNINGEN = "A_ORDNINGEN"
    }

    data class Sokeperiode(
        private val fomDate: LocalDate,
        private val tomDate: LocalDate
    ) {
        val fom: String get() = fomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tom: String get() = tomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
