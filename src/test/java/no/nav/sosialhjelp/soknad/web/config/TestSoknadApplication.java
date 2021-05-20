package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.web.oidc.TestTokenGeneratorResource;
import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication;

public class TestSoknadApplication extends SoknadApplication {

    public TestSoknadApplication() {
        register(TestTokenGeneratorResource.class); // Legger på endepunkter for å hente ut test-cookie til lokal kjøring
    }
}
