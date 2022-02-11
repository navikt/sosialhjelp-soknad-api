package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation

interface OrganisasjonClient {
    fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto?
}

class OrganisasjonClientImpl(
    private val client: Client,
    private val baseurl: String,
) : OrganisasjonClient {

    override fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto? {
        val request: Invocation.Builder = lagRequest("$baseurl/organisasjon/$orgnr")
        return try {
            request.get(OrganisasjonNoekkelinfoDto::class.java)
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

    private fun lagRequest(endpoint: String): Invocation.Builder {
        return client.target(endpoint)
            .request()
            .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID))
            .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
    }

    companion object {
        private val log = getLogger(OrganisasjonClientImpl::class.java)
    }
}
