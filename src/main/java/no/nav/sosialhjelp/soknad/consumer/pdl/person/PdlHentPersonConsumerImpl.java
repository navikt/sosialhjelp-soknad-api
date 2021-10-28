package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.pdl.BasePdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import java.util.Map;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_BARN;
import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_EKTEFELLE;
import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_PERSON;
import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_PERSON_ADRESSEBESKYTTELSE;
import static no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.BARN_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.EKTEFELLE_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.PDL_CACHE_SECONDS;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.PERSON_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_TEMA;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.TEMA_KOM;
import static org.slf4j.LoggerFactory.getLogger;

public class PdlHentPersonConsumerImpl extends BasePdlConsumer implements PdlHentPersonConsumer {

    private static final Logger log = getLogger(PdlHentPersonConsumerImpl.class);

    private final RedisService redisService;

    public PdlHentPersonConsumerImpl(Client client, String endpoint, STSConsumer stsConsumer, RedisService redisService) {
        super(client, endpoint, stsConsumer, log);
        this.redisService = redisService;
    }

    @Override
    public PdlPerson hentPerson(String ident) {
        return Optional.ofNullable(hentPersonFraCache(ident))
                .orElse(hentPersonFraPdl(ident));
    }

    private PdlPerson hentPersonFraCache(String ident) {
        return (PdlPerson) redisService.get(PERSON_CACHE_KEY_PREFIX + ident, PdlPerson.class);
    }

    private PdlPerson hentPersonFraPdl(String ident) {
        try {
            var response = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_PERSON, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentPersonResponse<PdlPerson>>() {});

            checkForPdlApiErrors(pdlResponse);

            var pdlPerson = pdlResponse.getData().getHentPerson();
            lagreTilCache(PERSON_CACHE_KEY_PREFIX, ident, pdlPerson);
            return pdlPerson;
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentPerson)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    public PdlBarn hentBarn(String ident) {
        return Optional.ofNullable(hentBarnFraCache(ident))
                .orElse(hentBarnFraPdl(ident));
    }

    private PdlBarn hentBarnFraCache(String ident) {
        return (PdlBarn) redisService.get(BARN_CACHE_KEY_PREFIX + ident, PdlBarn.class);
    }

    private PdlBarn hentBarnFraPdl(String ident) {
        try {
            var response = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_BARN, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentPersonResponse<PdlBarn>>() {});

            checkForPdlApiErrors(pdlResponse);

            var pdlBarn = pdlResponse.getData().getHentPerson();
            lagreTilCache(BARN_CACHE_KEY_PREFIX, ident, pdlBarn);
            return pdlBarn;
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentBarn)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    public PdlEktefelle hentEktefelle(String ident) {
        return Optional.ofNullable(hentEktefelleFraCache(ident))
                .orElse(hentEktefelleFraPdl(ident));
    }

    private PdlEktefelle hentEktefelleFraCache(String ident) {
        return (PdlEktefelle) redisService.get(EKTEFELLE_CACHE_KEY_PREFIX + ident, PdlEktefelle.class);
    }

    private PdlEktefelle hentEktefelleFraPdl(String ident) {
        try {
            var response = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_EKTEFELLE, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentPersonResponse<PdlEktefelle>>() {});

            checkForPdlApiErrors(pdlResponse);

            var pdlEktefelle = pdlResponse.getData().getHentPerson();
            lagreTilCache(EKTEFELLE_CACHE_KEY_PREFIX, ident, pdlEktefelle);
            return pdlEktefelle;
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentEktefelle)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    public PdlAdressebeskyttelse hentAdressebeskyttelse(String ident) {
        return Optional.ofNullable(hentAdressebeskyttelseFraCache(ident))
                .orElse(hentAdressebeskyttelseFraPdl(ident));
    }

    private PdlAdressebeskyttelse hentAdressebeskyttelseFraCache(String ident) {
        return (PdlAdressebeskyttelse) redisService.get(ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX + ident, PdlAdressebeskyttelse.class);
    }

    private PdlAdressebeskyttelse hentAdressebeskyttelseFraPdl(String ident) {
        try {
            var body = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_PERSON_ADRESSEBESKYTTELSE, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<HentPersonResponse<PdlAdressebeskyttelse>>() {});

            checkForPdlApiErrors(pdlResponse);

            var pdlAdressebeskyttelse = pdlResponse.getData().getHentPerson();
            lagreTilCache(ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX, ident, pdlAdressebeskyttelse);
            return pdlAdressebeskyttelse;
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentPersonAdressebeskyttelse)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    private Map<String, Object> variables(String ident) {
        return Map.of(
                "historikk", false,
                "ident", ident);
    }

    private Invocation.Builder hentPersonRequest(String endpoint) {
        return baseRequest(endpoint)
                .header(HEADER_TEMA, TEMA_KOM);
    }

    private void lagreTilCache(String prefix, String ident, Object pdlResponse) {
        try {
            redisService.setex(prefix + ident, pdlMapper.writeValueAsBytes(pdlResponse), PDL_CACHE_SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("Noe feilet ved serialisering av response fra Pdl - {}", pdlResponse.getClass().getName(), e);
        }
    }
}
