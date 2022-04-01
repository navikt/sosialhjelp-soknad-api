package no.nav.sosialhjelp.soknad.arbeid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServiceUnavailableException
import javax.ws.rs.client.Client
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.HttpHeaders.AUTHORIZATION

@Component
class ArbeidsforholdClient(
    @Value("\${aareg_proxy_url}") private val aaregProxyUrl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService
) {

    private val arbeidsforholdMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())

    private val arbeidsforholdClient: Client = RestUtils.createClient().register(arbeidsforholdMapper)

    private val callId: String? get() = MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID)
    private val sokeperiode: Sokeperiode get() = Sokeperiode(LocalDate.now().minusMonths(3), LocalDate.now())

    fun finnArbeidsforholdForArbeidstaker(fodselsnummer: String): List<ArbeidsforholdDto>? {
        try {
            return arbeidsforholdClient.target("${aaregProxyUrl}v1/arbeidstaker/arbeidsforhold")
                .queryParam("sporingsinformasjon", false)
                .queryParam("regelverk", A_ORDNINGEN)
                .queryParam("ansettelsesperiodeFom", sokeperiode.fom.format(ISO_LOCAL_DATE))
                .queryParam("ansettelsesperiodeTom", sokeperiode.tom.format(ISO_LOCAL_DATE))
                .request()
                .header(AUTHORIZATION, BEARER + tokenxToken)
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_NAV_PERSONIDENT, fodselsnummer)
                .get(object : GenericType<List<ArbeidsforholdDto>>() {})
        } catch (e: BadRequestException) {
            log.warn("Aareg.api - 400 Bad Request - Ugyldig(e) parameter(e) i request", e)
            return null
        } catch (e: NotAuthorizedException) {
            log.warn("Aareg.api - 401 Unauthorized - Token mangler eller er ugyldig", e)
            return null
        } catch (e: ForbiddenException) {
            log.warn("Aareg.api - 403 Forbidden - Ingen tilgang til forespurt ressurs", e)
            return null
        } catch (e: NotFoundException) {
            log.warn("Aareg.api - 404 Not Found - Fant ikke arbeidsforhold for bruker", e)
            return null
        } catch (e: ServiceUnavailableException) {
            log.error("Aareg.api - ${e.response.statusInfo} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: InternalServerErrorException) {
            log.error("Aareg.api - ${e.response.statusInfo} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: Exception) {
            log.error("Aareg.api - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        }
    }

    private val tokenxToken: String
        get() = runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), fssProxyAudience)
        }

    companion object {
        private val log = getLogger(ArbeidsforholdClient::class.java)
        private const val A_ORDNINGEN = "A_ORDNINGEN"
    }

    data class Sokeperiode(
        val fom: LocalDate,
        val tom: LocalDate
    )
}
