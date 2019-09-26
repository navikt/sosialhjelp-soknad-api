package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.time.LocalDate;

public class BostotteImpl implements Bostotte {
    private static final Logger logger = LoggerFactory.getLogger(BostotteImpl.class);
    private final BostotteConfig config;
    private final RestOperations operations;

    @Inject
    public BostotteImpl(BostotteConfig config, RestOperations operations) {
        this.config = config;
        this.operations = operations;
    }

    @Override
    public BostotteDto hentBostotte(String personIdentifikator, String token, LocalDate fra, LocalDate til) {
        try {
            UriBuilder uri = UriBuilder.fromPath(config.getUri()).queryParam("fra", fra).queryParam("til", til);
            RequestEntity<Void> request = RequestEntity.get(uri.build())
                    .header(config.getUsername(), config.getAppKey())
                    .header("Authorization", "Bearer " + token)
                    .build();
            return operations.exchange(request, BostotteDto.class).getBody();
        } catch (ResourceAccessException e) {
            logger.warn("Problemer med å hente bostøtte informasjon!", e);
        } catch (HttpClientErrorException e) {
            logger.warn("Problemer med å koble opp mot Husbanken!", e);
        }
        return null;
    }
}
