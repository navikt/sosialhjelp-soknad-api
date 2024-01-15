package no.nav.sosialhjelp.soknad.kodeverk

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.kodeverk.dto.KodeverkDto
import no.nav.sosialhjelp.soknad.redis.KODEVERK_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.redis.KODEVERK_LAST_POLL_TIME_KEY
import no.nav.sosialhjelp.soknad.redis.KOMMUNER_CACHE_KEY
import no.nav.sosialhjelp.soknad.redis.LANDKODER_CACHE_KEY
import no.nav.sosialhjelp.soknad.redis.POSTNUMMER_CACHE_KEY
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

@Component
class KodeverkClient(
    @Value("\${kodeverk_url}") private val kodeverkUrl: String,
    private val redisService: RedisService,
    webClientBuilder: WebClient.Builder,
) {

    private val kodeverkMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    private val webClient = unproxiedWebClientBuilder(webClientBuilder)
        .codecs {
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(kodeverkMapper))
        }
        .build()

    fun hentPostnummer(): KodeverkDto? {
        return hentKodeverk(POSTNUMMER, POSTNUMMER_CACHE_KEY)
    }

    fun hentKommuner(): KodeverkDto? {
        return hentKodeverk(KOMMUNER, KOMMUNER_CACHE_KEY)
    }

    fun hentLandkoder(): KodeverkDto? {
        return hentKodeverk(LANDKODER, LANDKODER_CACHE_KEY)
    }

    private fun hentKodeverk(kodeverksnavn: String, key: String): KodeverkDto? {
        return try {
            webClient.get()
                .uri(
                    "$kodeverkUrl/{kodeverksnavn}/koder/betydninger?ekskluderUgyldige={ekskluderUgyldige}&spraak={spraak}",
                    kodeverksnavn,
                    true,
                    SPRAAK_NB,
                )
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, getConsumerId())
                .retrieve()
                .bodyToMono<KodeverkDto>()
                .block()
                ?.also { oppdaterCache(key, it) }
        } catch (e: WebClientResponseException) {
            log.warn("Kodeverk - ${e.statusCode}", e)
            null
        } catch (e: Exception) {
            log.error("Kodeverk - noe uventet feilet", e)
            null
        }
    }

    private fun oppdaterCache(key: String, kodeverk: KodeverkDto) {
        try {
            redisService.setex(key, redisObjectMapper.writeValueAsBytes(kodeverk), KODEVERK_CACHE_SECONDS)
            redisService.set(
                KODEVERK_LAST_POLL_TIME_KEY,
                LocalDateTime.now().format(ISO_LOCAL_DATE_TIME).toByteArray(StandardCharsets.UTF_8),
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe galt skjedde ved oppdatering av kodeverk til Redis", e)
        }
    }

    companion object {
        private val log = getLogger(KodeverkClient::class.java)

        private const val SPRAAK_NB = "nb"

        private const val POSTNUMMER = "Postnummer"
        private const val KOMMUNER = "Kommuner"
        private const val LANDKODER = "Landkoder"
    }
}
