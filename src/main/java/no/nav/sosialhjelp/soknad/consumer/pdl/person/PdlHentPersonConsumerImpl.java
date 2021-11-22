//package no.nav.sosialhjelp.soknad.consumer.pdl.person;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import no.nav.sosialhjelp.soknad.client.pdl.HentPersonDto;
//import no.nav.sosialhjelp.soknad.client.redis.RedisService;
//import no.nav.sosialhjelp.soknad.client.sts.StsClient;
//import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
//import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
//import no.nav.sosialhjelp.soknad.consumer.pdl.BasePdlConsumer;
//import no.nav.sosialhjelp.soknad.person.dto.BarnDto;
//import no.nav.sosialhjelp.soknad.person.dto.EktefelleDto;
//import no.nav.sosialhjelp.soknad.person.dto.PersonAdressebeskyttelseDto;
//import no.nav.sosialhjelp.soknad.person.dto.PersonDto;
//import org.slf4j.Logger;
//
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Invocation;
//import java.util.Map;
//import java.util.Optional;
//
//import static no.nav.sosialhjelp.soknad.client.redis.CacheConstantsKt.ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX;
//import static no.nav.sosialhjelp.soknad.client.redis.CacheConstantsKt.BARN_CACHE_KEY_PREFIX;
//import static no.nav.sosialhjelp.soknad.client.redis.CacheConstantsKt.EKTEFELLE_CACHE_KEY_PREFIX;
//import static no.nav.sosialhjelp.soknad.client.redis.CacheConstantsKt.PDL_CACHE_SECONDS;
//import static no.nav.sosialhjelp.soknad.client.redis.CacheConstantsKt.PERSON_CACHE_KEY_PREFIX;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_BARN;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_EKTEFELLE;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_PERSON;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery.HENT_ADRESSEBESKYTTELSE;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_TEMA;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.TEMA_KOM;
//import static org.slf4j.LoggerFactory.getLogger;
//
//public class PdlHentPersonConsumerImpl extends BasePdlConsumer implements PdlHentPersonConsumer {
//
//    private static final Logger log = getLogger(PdlHentPersonConsumerImpl.class);
//
//    private final RedisService redisService;
//
//    public PdlHentPersonConsumerImpl(Client client, String endpoint, StsClient stsClient, RedisService redisService) {
//        super(client, endpoint, stsClient, log);
//        this.redisService = redisService;
//    }
//
//    @Override
//    public PersonDto hentPerson(String ident) {
//        return Optional.ofNullable(hentPersonFraCache(ident))
//                .orElse(hentPersonFraPdl(ident));
//    }
//
//    private PersonDto hentPersonFraCache(String ident) {
//        return (PersonDto) redisService.get(PERSON_CACHE_KEY_PREFIX + ident, PersonDto.class);
//    }
//
//    private PersonDto hentPersonFraPdl(String ident) {
//        try {
//            var response = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_PERSON, variables(ident)), String.class));
//            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentPersonDto<PersonDto>>() {});
//
//            pdlResponse.checkForPdlApiErrors();
//
//            var pdlPerson = pdlResponse.getData().getHentPerson();
//            lagreTilCache(PERSON_CACHE_KEY_PREFIX, ident, pdlPerson);
//            return pdlPerson;
//        } catch (PdlApiException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("Kall til PDL feilet (hentPerson)");
//            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
//        }
//    }
//
//    @Override
//    public BarnDto hentBarn(String ident) {
//        return Optional.ofNullable(hentBarnFraCache(ident))
//                .orElse(hentBarnFraPdl(ident));
//    }
//
//    private BarnDto hentBarnFraCache(String ident) {
//        return (BarnDto) redisService.get(BARN_CACHE_KEY_PREFIX + ident, BarnDto.class);
//    }
//
//    private BarnDto hentBarnFraPdl(String ident) {
//        try {
//            var response = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_BARN, variables(ident)), String.class));
//            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentPersonDto<BarnDto>>() {});
//
//            pdlResponse.checkForPdlApiErrors();
//
//            var pdlBarn = pdlResponse.getData().getHentPerson();
//            lagreTilCache(BARN_CACHE_KEY_PREFIX, ident, pdlBarn);
//            return pdlBarn;
//        } catch (PdlApiException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("Kall til PDL feilet (hentBarn)");
//            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
//        }
//    }
//
//    @Override
//    public EktefelleDto hentEktefelle(String ident) {
//        return Optional.ofNullable(hentEktefelleFraCache(ident))
//                .orElse(hentEktefelleFraPdl(ident));
//    }
//
//    private EktefelleDto hentEktefelleFraCache(String ident) {
//        return (EktefelleDto) redisService.get(EKTEFELLE_CACHE_KEY_PREFIX + ident, EktefelleDto.class);
//    }
//
//    private EktefelleDto hentEktefelleFraPdl(String ident) {
//        try {
//            var response = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_EKTEFELLE, variables(ident)), String.class));
//            var pdlResponse = pdlMapper.readValue(response, new TypeReference<HentPersonDto<EktefelleDto>>() {});
//
//            pdlResponse.checkForPdlApiErrors();
//
//            var pdlEktefelle = pdlResponse.getData().getHentPerson();
//            lagreTilCache(EKTEFELLE_CACHE_KEY_PREFIX, ident, pdlEktefelle);
//            return pdlEktefelle;
//        } catch (PdlApiException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("Kall til PDL feilet (hentEktefelle)");
//            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
//        }
//    }
//
//    @Override
//    public PersonAdressebeskyttelseDto hentAdressebeskyttelse(String ident) {
//        return Optional.ofNullable(hentAdressebeskyttelseFraCache(ident))
//                .orElse(hentAdressebeskyttelseFraPdl(ident));
//    }
//
//    private PersonAdressebeskyttelseDto hentAdressebeskyttelseFraCache(String ident) {
//        return (PersonAdressebeskyttelseDto) redisService.get(ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX + ident, PersonAdressebeskyttelseDto.class);
//    }
//
//    private PersonAdressebeskyttelseDto hentAdressebeskyttelseFraPdl(String ident) {
//        try {
//            var body = withRetry(() -> hentPersonRequest(endpoint).post(requestEntity(HENT_ADRESSEBESKYTTELSE, variables(ident)), String.class));
//            var pdlResponse = pdlMapper.readValue(body, new TypeReference<HentPersonDto<PersonAdressebeskyttelseDto>>() {});
//
//            pdlResponse.checkForPdlApiErrors();
//
//            var pdlAdressebeskyttelse = pdlResponse.getData().getHentPerson();
//            lagreTilCache(ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX, ident, pdlAdressebeskyttelse);
//            return pdlAdressebeskyttelse;
//        } catch (PdlApiException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("Kall til PDL feilet (hentPersonAdressebeskyttelse)");
//            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
//        }
//    }
//
//    private Map<String, Object> variables(String ident) {
//        return Map.of(
//                "historikk", false,
//                "ident", ident);
//    }
//
//    private Invocation.Builder hentPersonRequest(String endpoint) {
//        return baseRequest(endpoint)
//                .header(HEADER_TEMA, TEMA_KOM);
//    }
//
//    private void lagreTilCache(String prefix, String ident, Object pdlResponse) {
//        try {
//            redisService.setex(prefix + ident, pdlMapper.writeValueAsBytes(pdlResponse), PDL_CACHE_SECONDS);
//        } catch (JsonProcessingException e) {
//            log.error("Noe feilet ved serialisering av response fra Pdl - {}", pdlResponse.getClass().getName(), e);
//        }
//    }
//}
