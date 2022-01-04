package no.nav.sosialhjelp.soknad.client.kodeverk

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sosialhjelp.soknad.client.kodeverk.dto.KodeverkDto
import no.nav.sosialhjelp.soknad.client.redis.KODEVERK_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.KODEVERK_LAST_POLL_TIME_KEY
import no.nav.sosialhjelp.soknad.client.redis.KOMMUNER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.LANDKODER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.POSTNUMMER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID
import org.slf4j.LoggerFactory.getLogger
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import javax.ws.rs.ClientErrorException
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation

interface KodeverkClient {
    fun ping()
    fun hentPostnummer(): KodeverkDto?
    fun hentKommuner(): KodeverkDto?
    fun hentLandkoder(): KodeverkDto?
}

class KodeverkClientImpl(
    private val client: Client,
    private val baseurl: String,
    private val redisService: RedisService
) : KodeverkClient {

    override fun ping() {
        // Kaller GET /v1/kodeverk ettersom kodeverk ikke har dedikert ping-endepunkt
        lagRequest(URI.create(baseurl + "v1/kodeverk/")).get()
            .use { response ->
                if (response.status != 200) {
                    throw RuntimeException("Ping mot kodeverk feilet: ${response.status}, respons: ${response.readEntity(String::class.java)}")
                }
            }
    }

    override fun hentPostnummer(): KodeverkDto? {
        return hentKodeverk(POSTNUMMER, POSTNUMMER_CACHE_KEY)
    }

    override fun hentKommuner(): KodeverkDto? {
        return hentKodeverk(KOMMUNER, KOMMUNER_CACHE_KEY)
    }

    override fun hentLandkoder(): KodeverkDto? {
        return hentKodeverk(LANDKODER, LANDKODER_CACHE_KEY)
    }

    private fun lagRequest(uri: URI): Invocation.Builder {
        return client.target(uri)
            .queryParam("ekskluderUgyldige", true)
            .queryParam("spraak", "nb")
            .request()
            .header(HEADER_CALL_ID, MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID))
            .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
    }

    private fun hentKodeverk(kodeverksnavn: String, key: String): KodeverkDto? {
        return try {
            val kodeverk = lagRequest(kodeverkUri(kodeverksnavn)).get(KodeverkDto::class.java)
            oppdaterCache(key, kodeverk)
            kodeverk
        } catch (e: ClientErrorException) {
            log.warn("Kodeverk client-feil", e)
            null
        } catch (e: Exception) {
            log.error("Kodeverk - noe uventet feilet", e)
            null
        }
    }

    private fun kodeverkUri(kodeverksnavn: String): URI {
        return URI.create(baseurl + "v1/kodeverk/" + kodeverksnavn + "/koder/betydninger")
    }

    private fun oppdaterCache(key: String, kodeverk: KodeverkDto) {
        try {
            redisService.setex(key, redisObjectMapper.writeValueAsBytes(kodeverk), KODEVERK_CACHE_SECONDS)
            redisService.set(
                KODEVERK_LAST_POLL_TIME_KEY,
                LocalDateTime.now().format(ISO_LOCAL_DATE_TIME).toByteArray(StandardCharsets.UTF_8)
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe galt skjedde ved oppdatering av kodeverk til Redis", e)
        }
    }

    companion object {
        private val log = getLogger(KodeverkClientImpl::class.java)

        private const val POSTNUMMER = "Postnummer"
        private const val KOMMUNER = "Kommuner"
        private const val LANDKODER = "Landkoder"
    }
}
