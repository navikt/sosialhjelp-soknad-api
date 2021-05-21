package no.nav.sosialhjelp.soknad.web;

import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.oidc.OidcConfig;
import no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer;

import java.io.File;

public class DevSoknadsosialhjelpServer {

    public static final int PORT = 8181;

    public static void main(String[] args) throws Exception {
        SoknadsosialhjelpServer.setFrom("environment-test.properties", false);

        final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, new File("src/test/resources/override-web.xml"), "/sosialhjelp/soknad-api");
        if (OidcConfig.isOidcMock()) {
            SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        }

        server.start();
    }

}
