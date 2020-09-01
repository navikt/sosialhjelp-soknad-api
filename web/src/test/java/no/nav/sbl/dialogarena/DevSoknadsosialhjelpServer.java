package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.oidc.OidcConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

import javax.sql.DataSource;
import java.io.File;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.path.FilesAndDirs.TEST_RESOURCES;

public class DevSoknadsosialhjelpServer {

    public static final int PORT = 8181;

    public static void main(String[] args) throws Exception {
        SoknadsosialhjelpServer.setFrom("environment-test.properties", false);
        DataSource dataSource = null;


        if (System.getProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb").equals("true")) {
            dataSource = buildDataSource("hsqldb.properties");
        }

        final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, new File(TEST_RESOURCES, "override-web.xml"), "/sosialhjelp/soknad-api", dataSource);
        if (OidcConfig.isOidcMock()) {
            SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        }
        if ("Mac OS X".equals(System.getProperty("os.name")) || "Linux".equals(System.getProperty("os.name"))) {
            System.setProperty("sendsoknad.datadir", System.getProperty("user.home")+"/kodeverk/sendsoknad");
        }
        server.start();
    }

}
