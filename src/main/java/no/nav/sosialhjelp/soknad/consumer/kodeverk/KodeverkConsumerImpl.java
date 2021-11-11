//package no.nav.sosialhjelp.soknad.consumer.kodeverk;
//
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.KodeverkDto;
//import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
//import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import org.slf4j.Logger;
//
//import javax.ws.rs.ClientErrorException;
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Invocation;
//import javax.ws.rs.core.Response;
//import java.net.URI;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//
//import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KODEVERK_CACHE_SECONDS;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KODEVERK_LAST_POLL_TIME_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KOMMUNER_CACHE_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.LANDKODER_CACHE_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.POSTNUMMER_CACHE_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.restconfig.KodeverkRestConfig.kodeverkMapper;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
//import static org.slf4j.LoggerFactory.getLogger;
//
//public class KodeverkConsumerImpl implements KodeverkConsumer {
//
//    private static final Logger logger = getLogger(KodeverkConsumerImpl.class);
//
//    private static final String POSTNUMMER = "Postnummer";
//    private static final String KOMMUNER = "Kommuner";
//    private static final String LANDKODER = "Landkoder";
//
//    private final Client client;
//    private final String endpoint;
//    private final RedisService redisService;
//
//    public KodeverkConsumerImpl(Client client, String endpoint, RedisService redisService) {
//        this.client = client;
//        this.endpoint = endpoint;
//        this.redisService = redisService;
//    }
//
//    @Override
//    public void ping() {
//        // Kaller GET /v1/kodeverk ettersom kodeverk ikke har dedikert ping-endepunkt
//        var request = client.target(endpoint + "v1/kodeverk/").request();
//        try (Response response = addHeaders(request).get()) {
//            if (response.getStatus() != 200) {
//                throw new RuntimeException("Ping mot kodeverk feilet: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
//            }
//        }
//    }
//
//    @Override
//    public KodeverkDto hentPostnummer() {
//        return hentKodeverk(POSTNUMMER, POSTNUMMER_CACHE_KEY);
//    }
//
//    @Override
//    public KodeverkDto hentKommuner() {
//        return hentKodeverk(KOMMUNER, KOMMUNER_CACHE_KEY);
//    }
//
//    @Override
//    public KodeverkDto hentLandkoder() {
//        return hentKodeverk(LANDKODER, LANDKODER_CACHE_KEY);
//    }
//
//    private KodeverkDto hentKodeverk(String kodeverksnavn, String key) {
//        try {
//            var kodeverk = lagRequest(kodeverkUri(kodeverksnavn)).get(KodeverkDto.class);
//            oppdaterCache(key, kodeverk);
//            return kodeverk;
//        } catch (ClientErrorException e) {
//            logger.warn("Kodeverk client-feil", e);
//            return null;
//        } catch (Exception e) {
//            logger.error("Kodeverk - noe uventet feilet", e);
//            return null;
//        }
//    }
//
//    private Invocation.Builder lagRequest(URI uri) {
//        var request = client.target(uri)
//                .queryParam("ekskluderUgyldige", true)
//                .queryParam("spraak", "nb")
//                .request();
//
//        return addHeaders(request);
//    }
//
//    private Invocation.Builder addHeaders(Invocation.Builder request) {
//        var consumerId = SubjectHandler.getConsumerId();
//        var callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
//
//        return request
//                .header(HEADER_CALL_ID, callId)
//                .header(HEADER_CONSUMER_ID, consumerId);
//    }
//
//    private URI kodeverkUri(String kodeverksnavn) {
//        return URI.create(endpoint + "v1/kodeverk/" + kodeverksnavn + "/koder/betydninger");
//    }
//
//    private void oppdaterCache(String key, KodeverkDto kodeverk) {
//        try {
//            redisService.setex(key, kodeverkMapper().writeValueAsBytes(kodeverk), KODEVERK_CACHE_SECONDS);
//            redisService.set(KODEVERK_LAST_POLL_TIME_KEY, LocalDateTime.now().format(ISO_LOCAL_DATE_TIME).getBytes(StandardCharsets.UTF_8));
//        } catch (JsonProcessingException e) {
//            logger.warn("Noe galt skjedde ved oppdatering av kodeverk til Redis", e);
//        }
//    }
//}
