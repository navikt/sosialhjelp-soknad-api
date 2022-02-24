package no.nav.sosialhjelp.soknad.client.pdl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType

abstract class PdlClient(
    private val client: Client,
    private val baseurl: String,
) {

    private val callId: String? get() = MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID)

    open fun ping() {
        client
            .target(baseurl)
            .request()
            .header(HEADER_CALL_ID, callId)
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
            .header(HEADER_CALL_ID, callId)

    protected val pdlMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())
}
