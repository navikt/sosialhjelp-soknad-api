package no.nav.sosialhjelp.soknad.organisasjon

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client
import javax.ws.rs.core.HttpHeaders

interface OrganisasjonClient {
    fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto?
}

@Component
class OrganisasjonClientImpl(
    @Value("\${ereg_proxy_url}") private val baseurl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService
) : OrganisasjonClient {

    private val organisasjonClient: Client get() = RestUtils.createClient()

    override fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto? {
        return try {
            val tokenXtoken = runBlocking {
                tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), fssProxyAudience)
            }
            organisasjonClient.target("${baseurl}organisasjon/$orgnr")
                .request()
                .header(HttpHeaders.AUTHORIZATION, BEARER + tokenXtoken)
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
                .get(OrganisasjonNoekkelinfoDto::class.java)
        } catch (e: NotFoundException) {
            log.warn("Fss-proxy (ereg) - 404 Not Found - Fant ikke forespurt(e) entitet(er)")
            null
        } catch (e: BadRequestException) {
            log.warn("Fss-proxy (ereg) - 400 Bad Request - Ugyldig(e) parameter(e) i request")
            null
        } catch (e: ServerErrorException) {
            log.error("Fss-proxy (ereg) - ${e.response.status} ${e.response.statusInfo.reasonPhrase} - Tjenesten er utilgjengelig", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        } catch (e: Exception) {
            log.error("Fss-proxy (ereg) - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        }
    }

    companion object {
        private val log = getLogger(OrganisasjonClientImpl::class.java)
    }
}
