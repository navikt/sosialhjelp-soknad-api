package no.nav.sosialhjelp.soknad.consumer.virusscan;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Configuration
public class VirusScanConfig {

    private static final URI DEFAULT_CLAM_URI = URI.create("http://clamav.nais.svc.nais.local/scan");

    @Bean
    public VirusScanner virusScanner(WebClient webClient) {
        final var clamAvWebClient = webClient.mutate()
                .baseUrl(DEFAULT_CLAM_URI.toString())
                .build();

        return new ClamAvVirusScanner(DEFAULT_CLAM_URI, clamAvWebClient);
    }
}
