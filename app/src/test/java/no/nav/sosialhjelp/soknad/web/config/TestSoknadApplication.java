package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication;
import no.nav.sosialhjelp.soknad.web.oidc.MockLoginServiceRessurs;

public class TestSoknadApplication extends SoknadApplication {

    public TestSoknadApplication() {
        register(MockLoginServiceRessurs.class); // Legger på endepunkter for å hente ut test-cookie til lokal kjøring
    }
}
