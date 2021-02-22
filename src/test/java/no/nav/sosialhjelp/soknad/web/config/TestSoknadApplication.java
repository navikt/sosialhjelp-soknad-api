package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.web.oidc.MockLoginServiceRessurs;
import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication;

public class TestSoknadApplication extends SoknadApplication {

    public TestSoknadApplication() {
        register(MockLoginServiceRessurs.class); // Legger på endepunkter for å hente ut test-cookie til lokal kjøring
    }
}
