package no.nav.sosialhjelp.soknad.client.pdl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.client.sts.StsClient
import no.nav.sosialhjelp.soknad.client.sts.dto.FssToken
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import org.eclipse.jetty.http.HttpHeader
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType

abstract class PdlClient(
    private val client: Client,
    private val baseurl: String,
    private val stsClient: StsClient
) {

    private val callId: String? get() = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID)
    private val consumerId: String? get() = SubjectHandler.getConsumerId()
    private val fssToken: FssToken get() = stsClient.getFssToken()

    open fun ping() {
        client
            .target(baseurl)
            .request()
            .header(HeaderConstants.HEADER_CALL_ID, callId)
            .header(HeaderConstants.HEADER_CONSUMER_ID, consumerId)
            .options()
            .use { response ->
                if (response.status != 200) {
                    throw RuntimeException("PDL - ping feiler: ${response.status}, respons: ${response.readEntity(String::class.java)}")
                }
            }
    }

    protected open fun requestEntity(query: String, variables: Map<String, Any>): Entity<PdlRequest> {
        return Entity.entity(
            PdlRequest(query, variables),
            MediaType.APPLICATION_JSON_TYPE
        )
    }

    protected val baseRequest: Invocation.Builder
        get() = client.target(baseurl)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .header(HttpHeader.AUTHORIZATION.name, HeaderConstants.BEARER + fssToken.access_token)
            .header(HeaderConstants.HEADER_CALL_ID, callId)
            .header(HeaderConstants.HEADER_CONSUMER_ID, consumerId)
            .header(HeaderConstants.HEADER_CONSUMER_TOKEN, HeaderConstants.BEARER + fssToken.access_token)

    protected val pdlMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())
}
