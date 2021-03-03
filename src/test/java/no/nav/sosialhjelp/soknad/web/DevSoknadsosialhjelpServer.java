package no.nav.sosialhjelp.soknad.web;

import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.oidc.OidcConfig;
import no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer;

import javax.sql.DataSource;
import java.io.File;

import static no.nav.sosialhjelp.soknad.business.db.config.DatabaseTestContext.buildDataSource;

public class DevSoknadsosialhjelpServer {

    public static final int PORT = 8181;

    public static void main(String[] args) throws Exception {
        SoknadsosialhjelpServer.setFrom("environment-test.properties", false);
        DataSource dataSource = null;


        if (System.getProperty("no.nav.sosialhjelp.soknad.hsqldb").equals("true")) {
            dataSource = buildDataSource("hsqldb.properties");
        }

        final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, new File("src/test/resources/override-web.xml"), "/sosialhjelp/soknad-api", dataSource);
        if (OidcConfig.isOidcMock()) {
            SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        }

        server.start();
    }

}
