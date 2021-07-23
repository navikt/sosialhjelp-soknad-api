package no.nav.sosialhjelp.soknad;

import no.nav.sosialhjelp.soknad.web.config.PdlIntegrationTestConfig;
import no.nav.sosialhjelp.soknad.web.config.SoknadinnsendingConfig;
import no.nav.sosialhjelp.soknad.web.config.SoknadinnsendingLocalConfig;
import no.nav.sosialhjelp.soknad.web.oidc.OidcConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(
        classes = {Application.class, SoknadinnsendingConfig.class, SoknadinnsendingLocalConfig.class, PdlIntegrationTestConfig.class, OidcConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = {"test","no-redis"})
class TestApplication {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private Integer port;

    @Test
    void isAlive() {
        ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/sosialhjelp/soknad-api/internal/isAlive", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
