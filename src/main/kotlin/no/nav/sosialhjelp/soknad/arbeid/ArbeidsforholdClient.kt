package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.BEARER
import org.eclipse.jetty.http.HttpHeader
import org.slf4j.LoggerFactory.getLogger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServiceUnavailableException
import javax.ws.rs.client.Client
import javax.ws.rs.core.GenericType

interface ArbeidsforholdClient {
    fun ping()
    fun finnArbeidsforholdForArbeidstaker(fodselsnummer: String): List<ArbeidsforholdDto>?
}

class ArbeidsforholdClientImpl(
    private val client: Client,
    private val baseurl: String,
    private val stsConsumer: STSConsumer
) : ArbeidsforholdClient {

    private val callId: String? get() =  MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID)
    private val consumerId: String? get() = SubjectHandler.getConsumerId()
    private val userToken: String? get() = SubjectHandler.getToken()
    private val sokeperiode: Sokeperiode get() = Sokeperiode(LocalDate.now().minusMonths(3), LocalDate.now())

    override fun ping() {
        client.target(baseurl + "ping")
            .request()
            .header(HeaderConstants.HEADER_CALL_ID, callId)
            .header(HeaderConstants.HEADER_CONSUMER_ID, consumerId)
            .options().use { response ->
            if (response.status != 200) {
                throw RuntimeException("Aareg.api - Feil statuskode ved ping: ${response.status}, respons: ${response.readEntity(String::class.java)}")
            }
        }
    }

    override fun finnArbeidsforholdForArbeidstaker(fodselsnummer: String): List<ArbeidsforholdDto>? {
        try {
            return client.target(baseurl + "v1/arbeidstaker/arbeidsforhold")
                .queryParam("sporingsinformasjon", false)
                .queryParam("regelverk", A_ORDNINGEN)
                .queryParam("ansettelsesperiodeFom", sokeperiode.fom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam("ansettelsesperiodeTom", sokeperiode.tom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .request()
                .header(HttpHeader.AUTHORIZATION.name, BEARER + userToken) // brukers token
                .header(HeaderConstants.HEADER_CALL_ID, callId)
                .header(HeaderConstants.HEADER_CONSUMER_ID, consumerId)
                .header(HeaderConstants.HEADER_CONSUMER_TOKEN, BEARER + stsConsumer.fssToken.accessToken)
                .header(HeaderConstants.HEADER_NAV_PERSONIDENT, fodselsnummer)
                .get(object : GenericType<List<ArbeidsforholdDto>>() {})
        } catch (e: BadRequestException) {
            log.warn("Aareg.api - 400 Bad Request - Ugyldig(e) parameter(e) i request", e)
            return null
        } catch (e: NotAuthorizedException) {
            log.warn("Aareg.api - 401 Unauthorized- Token mangler eller er ugyldig", e)
            return null
        } catch (e: ForbiddenException){
            log.warn("Aareg.api - 403 Forbidden - Ingen tilgang til forespurt ressurs", e)
            return null
        } catch (e: NotFoundException) {
            log.warn("Aareg.api - 404 Not Found- Fant ikke arbeidsforhold for bruker", e)
            return null
        } catch (e: ServiceUnavailableException) {
            log.error("Aareg.api - ${e.response.status} ${e.response.statusInfo.reasonPhrase} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: InternalServerErrorException) {
            log.error("Aareg.api - ${e.response.status} ${e.response.statusInfo.reasonPhrase} - Tjenesten er ikke tilgjengelig", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        } catch (e: Exception) {
            log.error("Aareg.api - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("AAREG", e)
        }
    }

    companion object {
        private val log = getLogger(ArbeidsforholdClientImpl::class.java)
        private const val A_ORDNINGEN = "A_ORDNINGEN"
    }

    data class Sokeperiode(
        val fom: LocalDate,
        val tom: LocalDate
    )
}
