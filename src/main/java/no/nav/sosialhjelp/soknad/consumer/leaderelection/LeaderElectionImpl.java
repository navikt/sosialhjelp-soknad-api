package no.nav.sosialhjelp.soknad.consumer.leaderelection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import static java.net.InetAddress.getLocalHost;
import static org.slf4j.LoggerFactory.getLogger;

@Profile("!no-leader-election")
@Component
public class LeaderElectionImpl implements LeaderElection {

    private static final String ELECTOR_PATH = "ELECTOR_PATH";
    private static final Logger log = getLogger(LeaderElectionImpl.class);

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String hostname;
    private String leader;
    private LocalDateTime lastCallTime = LocalDateTime.MIN;

    public LeaderElectionImpl(WebClient webClient) throws UnknownHostException {
        this.webClient = webClient;
        this.hostname = getLocalHost().getHostName();
    }

    @Override
    public boolean isLeader() {
        String electorPath = System.getenv(ELECTOR_PATH);
        if (electorPath == null) {
            log.warn("LeaderElection - manglende systemvariabel={}.", ELECTOR_PATH);
            return true;
        }
        if (leader == null || lastCallTime.isBefore(LocalDateTime.now().minusMinutes(2))){
            try {
                final var response = webClient.get()
                        .uri(URI.create("http://" + electorPath).toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                leader = mapper.readTree(response).get("name").asText();
                lastCallTime = LocalDateTime.now();
            } catch (Exception e) {
                log.warn("LeaderElection - kunne ikke bestemme lederpod. {}", e.getMessage(), e);
                return true;
            }
        }
        return hostname.equals(leader);
    }
}
