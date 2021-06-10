package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.pdl.BasePdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery;
import no.nav.sosialhjelp.soknad.consumer.sts.apigw.STSConsumer;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import java.util.Map;

import static no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_TEMA;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.TEMA_KOM;
import static org.slf4j.LoggerFactory.getLogger;

public class PdlHentPersonConsumerImpl extends BasePdlConsumer implements PdlHentPersonConsumer {

    private static final Logger log = getLogger(PdlHentPersonConsumerImpl.class);

    public PdlHentPersonConsumerImpl(Client client, String endpoint, STSConsumer stsConsumer) {
        super(client, endpoint, stsConsumer, log);
    }

    @Override
    @Cacheable(value = "pdlPersonCache", key = "#ident")
    public PdlPerson hentPerson(String ident) {
        String query = PdlApiQuery.HENT_PERSON;
        try {
            var request = hentPersonRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(query, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<HentPersonResponse<PdlPerson>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentPerson)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    @Cacheable(value = "pdlBarnCache", key = "#ident")
    public PdlBarn hentBarn(String ident) {
        String query = PdlApiQuery.HENT_BARN;
        try {
            var request = hentPersonRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(query, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<HentPersonResponse<PdlBarn>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentBarn)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    @Cacheable(value = "pdlEktefelleCache", key = "#ident")
    public PdlEktefelle hentEktefelle(String ident) {
        String query = PdlApiQuery.HENT_EKTEFELLE;
        try {
            var request = hentPersonRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(query, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<HentPersonResponse<PdlEktefelle>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentEktefelle)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    @Cacheable(value = "pdlAdressebeskyttelseCache", key = "#ident")
    public PdlAdressebeskyttelse hentAdressebeskyttelse(String ident) {
        String query = PdlApiQuery.HENT_PERSON_ADRESSEBESKYTTELSE;
        try {
            var request = hentPersonRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(query, variables(ident)), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<HentPersonResponse<PdlAdressebeskyttelse>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
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
}
