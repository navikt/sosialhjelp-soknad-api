package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.oidc.MockLoginServiceRessurs;
import no.nav.sbl.dialogarena.rest.SoknadApplication;

public class TestSoknadApplication extends SoknadApplication {

    public TestSoknadApplication() {
        register(MockLoginServiceRessurs.class); // Legger på endepunkter for å hente ut test-cookie til lokal kjøring
    }
}
