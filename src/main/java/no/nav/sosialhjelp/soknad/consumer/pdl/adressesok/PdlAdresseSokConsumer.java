package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto;
import no.nav.sosialhjelp.soknad.client.pdl.AdressesokDto;
import no.nav.sosialhjelp.soknad.client.sts.StsClient;
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.pdl.BasePdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import java.util.Map;

import static no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper;
import static org.slf4j.LoggerFactory.getLogger;

public class PdlAdresseSokConsumer extends BasePdlConsumer {

    private static final Logger log = getLogger(PdlAdresseSokConsumer.class);

    public PdlAdresseSokConsumer(Client client, String endpoint, StsClient stsClient) {
        super(client, endpoint, stsClient, log);
    }

    public AdressesokResultDto getAdresseSokResult(Map<String, Object> variables) {
        var query = PdlApiQuery.ADRESSE_SOK;
        try {
            var request = adresseSokRequest(endpoint);
            var requestEntity = requestEntity(query, variables);
            var body = withRetry(() -> request.post(requestEntity, String.class));

            var pdlResponse = pdlMapper.readValue(body, new TypeReference<AdressesokDto>() {});

            pdlResponse.checkForPdlApiErrors();

            return pdlResponse.getData().getSokAdresse();
        } catch (PdlApiException e) {
            log.warn("PDL - feil oppdaget i response: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Kall til PDL feilet (adresseSok)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    private Invocation.Builder adresseSokRequest(String endpoint) {
        return baseRequest(endpoint);
    }
}
