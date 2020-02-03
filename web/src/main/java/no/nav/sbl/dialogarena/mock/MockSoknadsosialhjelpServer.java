package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

import java.io.File;
import java.util.Objects;

public class MockSoknadsosialhjelpServer {

    private static final int PORT = isRunningOnGCP() ? Integer.parseInt(System.getenv("PORT")) : 8181;

    public static void main(String[] args) throws Exception {
        System.setProperty("tillatMockRessurs", "true");
        System.setProperty("logback.configurationFile", "logback-mock.xml");

        File override = new File(Objects.requireNonNull(MockSoknadsosialhjelpServer.class.getClassLoader().getResource("override-web-mock.xml")).getFile());
        SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, override, "/sosialhjelp/soknad-api", null);

        if (!isRunningOnGCP()) {
            System.setProperty("sendsoknad.datadir", System.getProperty("user.home") + "/kodeverk/sendsoknad");
        }

        server.start();
    }

    private static boolean isRunningOnGCP(){
        return System.getenv("GCP") != null && Boolean.parseBoolean(System.getenv("GCP"));
    }
}
