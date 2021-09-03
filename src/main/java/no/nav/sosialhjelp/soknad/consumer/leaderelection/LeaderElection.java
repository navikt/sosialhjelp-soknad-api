package no.nav.sosialhjelp.soknad.consumer.leaderelection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.UnknownHostException;
import java.time.LocalDateTime;

import static java.net.InetAddress.getLocalHost;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class LeaderElection {

    private static final String ELECTOR_PATH = "ELECTOR_PATH";
    private static final Logger log = getLogger(LeaderElection.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String hostname;
    private String leader;
    private LocalDateTime lastCallTime = LocalDateTime.MIN;

    public LeaderElection() throws UnknownHostException {
        this.restTemplate = new RestTemplate(getClientHttpRequestFactory());
        this.hostname = getLocalHost().getHostName();
    }

    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() {
        var clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(5_000);
        clientHttpRequestFactory.setReadTimeout(20_000);
        return clientHttpRequestFactory;
    }

    public boolean isLeader() {
        String electorPath = System.getenv(ELECTOR_PATH);
        if (electorPath == null) {
            log.warn("LeaderElection - manglende systemvariabel={}.", ELECTOR_PATH);
            return true;
        }
        if (leader == null || lastCallTime.isBefore(LocalDateTime.now().minusMinutes(2))){
            try {
                String response = restTemplate.getForObject("http://" + electorPath, String.class);
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
