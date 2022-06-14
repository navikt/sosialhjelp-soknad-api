package no.nav.sosialhjelp.soknad.arbeid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@Component
class ArbeidsforholdClient(
    @Value("\${aareg_proxy_url}") private val aaregProxyUrl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder,
) {

    private val arbeidsforholdMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())

    private val callId: String? get() = MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID)
    private val sokeperiode: Sokeperiode get() = Sokeperiode(LocalDate.now().minusMonths(3), LocalDate.now())
    private val tokenxToken: String get() = runBlocking {
        tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), fssProxyAudience)
    }

    private val webClient = unproxiedWebClientBuilder(webClientBuilder)
        .baseUrl(aaregProxyUrl)
        .codecs {
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(arbeidsforholdMapper))
        }
        .build()

    private val queryParamsPart = "?sporingsinformasjon={sporingsinformasjon}&regelverk={regelverk}&ansettelsesperiodeFom={fom}&ansettelsesperiodeTom={tom}"

    fun finnArbeidsforholdForArbeidstaker(fodselsnummer: String): List<ArbeidsforholdDto>? {
        try {
            return webClient.get()
                .uri("v1/arbeidstaker/arbeidsforhold$queryParamsPart", false, A_ORDNINGEN, sokeperiode.fom, sokeperiode.tom)
                .header(AUTHORIZATION, BEARER + tokenxToken)
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_NAV_PERSONIDENT, fodselsnummer)
                .retrieve()
                .bodyToMono<List<ArbeidsforholdDto>>()
                .block()
        } catch (e: BadRequest) {
            log.warn("Aareg.api - 400 Bad Request - Ugyldig(e) parameter(e) i request", e)
            return null
        } catch (e: Unauthorized) {
            log.warn("Aareg.api - 401 Unauthorized - Token mangler eller er ugyldig", e)
            return null
        } catch (e: Forbidden) {
            log.warn("Aareg.api - 403 Forbidden - Ingen tilgang til forespurt ressurs", e)
            return null
        } catch (e: NotFound) {
            log.warn("Aareg.api - 404 Not Found - Fant ikke arbeidsforhold for bruker", e)
            return null
        } catch (e: ServiceUnavailable) {
            log.error("Aareg.api - ${e.statusCode} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: InternalServerError) {
            log.error("Aareg.api - ${e.statusCode} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: Exception) {
            log.error("Aareg.api - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        }
    }

    companion object {
        private val log = getLogger(ArbeidsforholdClient::class.java)
        private const val A_ORDNINGEN = "A_ORDNINGEN"
    }

    data class Sokeperiode(
        private val fomDate: LocalDate,
        private val tomDate: LocalDate
    ) {
        val fom: String get() = fomDate.format(ISO_LOCAL_DATE)
        val tom: String get() = tomDate.format(ISO_LOCAL_DATE)
    }
}
