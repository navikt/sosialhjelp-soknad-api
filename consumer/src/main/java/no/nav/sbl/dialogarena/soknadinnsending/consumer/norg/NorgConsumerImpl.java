package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

public class NorgConsumerImpl implements NorgConsumer {

    private static final Logger logger = getLogger(NorgConsumerImpl.class);

    private Client client;
    private String endpoint;

    public NorgConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public RsNorgEnhet finnEnhetForGeografiskTilknytning(String geografiskTilknytning) {
        Response response = null;

        try {
            response = client.target(endpoint + "/enhet/navkontor/" + geografiskTilknytning)
                    .request().get();
            return response.readEntity(RsNorgEnhet.class);
        } catch (NotFoundException e) {
            logger.warn("Fant ikke norgenhet for gt {}", geografiskTilknytning);
            return null;
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til NORG/gt", e);
            throw new TjenesteUtilgjengeligException("NORG", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public RsKontaktinformasjon hentKontaktinformasjonForEnhet(String enhetNr) {
        Response response = null;

        try {
            response = client.target(endpoint + "/enhet/" + enhetNr + "/kontaktinformasjon")
                    .request().get();
            return response.readEntity(RsKontaktinformasjon.class);
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til NORG/kontaktinformasjon", e);
            throw new TjenesteUtilgjengeligException("NORG", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
