package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.types.Pingable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.time.LocalDate;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

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
                    .header("Authorization", token)
                    .build();
            return operations.exchange(request, BostotteDto.class).getBody();
        } catch (ResourceAccessException e) {
            logger.error("Problemer med å hente bostøtte informasjon!", e);
        } catch (HttpClientErrorException e) {
            logger.error("Problemer med å koble opp mot Husbanken!", e);
        } catch (HttpServerErrorException e) {
            logger.error("Problemer med å hente bostøtte fra Husbanken! Ekstern error: " + e.getMessage(), e);
        } catch (HttpMessageNotReadableException e) {
            logger.error("Problemer med å tolke data fra Husbanken!", e);
        }
        return null;
    }

    static Pingable opprettHusbankenPing(BostotteConfig config, RestOperations operations) {
        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(config.getPingUrl(), "Husbanken API", false);

            @Override
            public Ping ping() {
                try {
                    RequestEntity<Void> request = RequestEntity.get(UriBuilder.fromPath(config.getPingUrl()).build())
                            .header(config.getUsername(), config.getAppKey())
                            .build();
                    String result = operations.exchange(request, String.class).getBody();
                    if (result.equalsIgnoreCase("pong")) {
                        return lyktes(metadata);
                    }
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
                return feilet(metadata, "Feil ping svar fra Husbanken!");
            }
        };
    }
}
