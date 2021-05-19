package no.nav.sosialhjelp.soknad.consumer.bostotte;

import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.BostotteDto;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.eclipse.jetty.http.HttpHeader;
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

import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

@Timed
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
                    .header(HttpHeader.AUTHORIZATION.name(), token)
                    .build();
            BostotteDto bostotteDto = operations.exchange(request, BostotteDto.class).getBody();
            logger.info("Hentet bostøtte informasjon fra Husbanken!");
            return bostotteDto;
        } catch (ResourceAccessException e) {
            logger.error("Problemer med å hente bostøtte informasjon fra Husbanken!", e);
        } catch (HttpClientErrorException e) {
            logger.error("Problemer med å koble opp mot Husbanken!", e);
        } catch (HttpServerErrorException e) {
            logger.error("Problemer med å hente bostøtte fra Husbanken! Ekstern error: {}", e.getMessage(), e);
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
