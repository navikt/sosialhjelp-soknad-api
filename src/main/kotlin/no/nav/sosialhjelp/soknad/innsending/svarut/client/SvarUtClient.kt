package no.nav.sosialhjelp.soknad.innsending.svarut.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.ks.fiks.svarut.klient.model.Forsendelse
import no.ks.fiks.svarut.klient.model.ForsendelsesId
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.rest.RestConfig
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import java.io.InputStream
import java.nio.charset.StandardCharsets
import javax.ws.rs.ClientErrorException
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE
import javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE
import javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE
import javax.xml.bind.DatatypeConverter

interface SvarUtClient {
    fun ping()
    fun sendForsendelse(forsendelse: Forsendelse?, data: Map<String, InputStream>?): ForsendelsesId
}

@Component
class SvarUtClientImpl(
    @Value("\${svarut_url}") private var baseurl: String,
    @Value("\${fiks_svarut_username}") private val svarutUsername: String?,
    @Value("\${fiks_svarut_password}") private val svarutPassword: String?,
) : SvarUtClient {

    private val basicAuthentication: String
        get() {
            if (svarutUsername == null || svarutPassword == null) {
                throw RuntimeException("svarutUsername eller svarutPassword er ikke tilgjengelig.")
            }
            val token = "$svarutUsername:$svarutPassword"
            return "Basic " + DatatypeConverter.printBase64Binary(token.toByteArray(StandardCharsets.UTF_8))
        }

    private val client: Client = RestUtils
        .createClient(RestConfig(connectTimeout = SVARUT_TIMEOUT, readTimeout = SVARUT_TIMEOUT))
        .register(MultiPartFeature::class.java)
        .register(ClientRequestFilter { it.headers.putSingle(HttpHeaders.AUTHORIZATION, basicAuthentication) })

    override fun ping() {
        client
            .target("$baseurl/tjenester/api/forsendelse/v1/forsendelseTyper")
            .request()
            .get().use { response ->
                if (response.status != 200) {
                    log.warn("Ping feilet mot SvarUt. ${response.statusInfo}")
                }
            }
    }

    override fun sendForsendelse(forsendelse: Forsendelse?, data: Map<String, InputStream>?): ForsendelsesId {
        requireNotNull(forsendelse) { "Forsendelse kan ikke være null" }
        requireNotNull(data) { "Data kan ikke være null" }
        return try {
            val multiPart = MultiPart()
            multiPart.mediaType = MULTIPART_FORM_DATA_TYPE
            multiPart.bodyPart(
                FormDataBodyPart("forsendelse", objectMapper.writeValueAsString(forsendelse), APPLICATION_JSON_TYPE)
            )
            forsendelse.dokumenter
                .forEach {
                    multiPart.bodyPart(
                        StreamDataBodyPart("filer", data[it.filnavn], it.filnavn, APPLICATION_OCTET_STREAM_TYPE)
                    )
                }
            multiPart.close()
            val response = client
                .target("$baseurl/tjenester/api/forsendelse/v1/sendForsendelse")
                .request(APPLICATION_JSON_TYPE)
                .post(Entity.entity(multiPart, multiPart.mediaType), String::class.java)
            objectMapper.readValue(response)
        } catch (e: ClientErrorException) {
            throw e
        } catch (e: Exception) {
            throw TjenesteUtilgjengeligException("Noe feilet ved kall til SvarUt (rest)", e)
        }
    }

    companion object {
        private val log = getLogger(SvarUtClientImpl::class.java)
        private val objectMapper = jacksonObjectMapper()

        private const val SVARUT_TIMEOUT = 16 * 60 * 1000
    }
}
