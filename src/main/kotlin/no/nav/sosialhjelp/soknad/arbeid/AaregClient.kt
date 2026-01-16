package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class AaregClient(
    @param:Value("\${aareg_url}") private val aaregUrl: String,
    @param:Value("\${aareg_audience}") private val aaregAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val sokeperiode: Sokeperiode get() = Sokeperiode(LocalDate.now().minusMonths(3), LocalDate.now())

    private val tokenxToken: String get() =
        texasService.exchangeToken(IdentityProvider.TOKENX, target = aaregAudience)

    private val webClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient())
            .baseUrl(aaregUrl)
            .codecs { it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(soknadJacksonMapper)) }
            .build()

    private val queryParamsPart = "?sporingsinformasjon={sporingsinformasjon}&regelverk={regelverk}&ansettelsesperiodeFom={fom}&ansettelsesperiodeTom={tom}"

    fun finnArbeidsforholdForArbeidstaker(): List<ArbeidsforholdDto>? {
        try {
            return webClient.get()
                .uri("/v1/arbeidstaker/arbeidsforhold$queryParamsPart", false, A_ORDNINGEN, sokeperiode.fom, sokeperiode.tom)
                .header(AUTHORIZATION, BEARER + tokenxToken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID) ?: "")
                .header(HEADER_NAV_PERSONIDENT, getUserIdFromToken())
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

    companion object {
        private val log by logger()
        private const val A_ORDNINGEN = "A_ORDNINGEN"
    }

    data class Sokeperiode(
        private val fomDate: LocalDate,
        private val tomDate: LocalDate,
    ) {
        val fom: String get() = fomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tom: String get() = tomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
