package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.rest.SoknadApplication;
import no.nav.sbl.dialogarena.oidc.MockLoginServiceResource;

public class TestSoknadApplication extends SoknadApplication {

    public TestSoknadApplication() {
        register(MockLoginServiceResource.class); // Legger på endepunkter for å hente ut test-cookie til lokal kjøring
    }
}
