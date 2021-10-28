package no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.pdl.BasePdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto.GeografiskTilknytningDto;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_GEOGRAFISK_TILKNYTNING;
import static no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.CACHE_30_MINUTES_IN_SECONDS;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheType.HENT_GEOGRAFISKTILKNYTNING;
import static no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils.cacheKey;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_TEMA;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.TEMA_KOM;
import static org.slf4j.LoggerFactory.getLogger;

public class GeografiskTilknytningConsumer extends BasePdlConsumer {

    private static final Logger log = getLogger(GeografiskTilknytningConsumer.class);

    private final RedisService redisService;

    public GeografiskTilknytningConsumer(Client client, String endpoint, STSConsumer stsConsumer, RedisService redisService) {
        super(client, endpoint, stsConsumer, log);
        this.redisService = redisService;
    }

    public GeografiskTilknytningDto hentGeografiskTilknytning(String ident) {
        return Optional.ofNullable(hentFraCache(ident))
                .orElse(hentFraPdl(ident));
    }

    private GeografiskTilknytningDto hentFraCache(String ident) {
        return (GeografiskTilknytningDto) redisService.get(cacheKey(HENT_GEOGRAFISKTILKNYTNING, ident), GeografiskTilknytningDto.class);
    }

    private GeografiskTilknytningDto hentFraPdl(String ident) {
        try {
            var response = withRetry(() -> request(endpoint).post(requestEntity(HENT_GEOGRAFISK_TILKNYTNING, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentGeografiskTilknytningResponse>() {});

            checkForPdlApiErrors(pdlResponse);

            var geografiskTilknytning = pdlResponse.getData().getGeografiskTilknytning();
            lagreTilCache(ident, geografiskTilknytning);
            return geografiskTilknytning;
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentGeografiskTilknytning)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    private Map<String, Object> variables(String ident) {
        return singletonMap("ident", ident);
    }

    private Invocation.Builder request(String endpoint) {
        return baseRequest(endpoint)
                .header(HEADER_TEMA, TEMA_KOM);
    }

    private void lagreTilCache(String ident, GeografiskTilknytningDto geografiskTilknytningDto) {
        try {
            redisService.setex(cacheKey(HENT_GEOGRAFISKTILKNYTNING, ident), pdlMapper.writeValueAsBytes(geografiskTilknytningDto), CACHE_30_MINUTES_IN_SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("Noe feilet ved serialisering av geografiskTilknytningDto fra Pdl - {}", geografiskTilknytningDto.getClass().getName(), e);
        }
    }
}
