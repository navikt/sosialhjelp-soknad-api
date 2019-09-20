package no.nav.sbl.dialogarena.bostotte;

import no.nav.sbl.dialogarena.bostotte.dto.BostotteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
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
    public BostotteDto hentBostotte(String personIdentifikator, LocalDate fra, LocalDate til) {
        UriBuilder uri = UriBuilder.fromPath(config.getUri()).queryParam("fra", fra).queryParam("til", til);
        return operations.exchange(RequestEntity.put(uri.build()).build(), BostotteDto.class).getBody();
    }
}
