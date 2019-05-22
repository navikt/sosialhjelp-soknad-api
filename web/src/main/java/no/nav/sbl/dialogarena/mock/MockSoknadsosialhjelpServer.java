package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

import java.io.File;
import java.util.Objects;

public class MockSoknadsosialhjelpServer {

    private static final int PORT = isRunningOnHeroku() ? Integer.parseInt(System.getenv("PORT")) : 8181;

    public static void main(String[] args) throws Exception {
        System.setProperty("tillatMockRessurs", "true");

        File override = new File(Objects.requireNonNull(MockSoknadsosialhjelpServer.class.getClassLoader().getResource("override-web-mock.xml")).getFile());
        SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, override, "/soknadsosialhjelp-server", null);

        if (!isRunningOnHeroku()) {
            System.setProperty("sendsoknad.datadir", System.getProperty("user.home") + "/kodeverk/sendsoknad");
        }

        server.start();
    }

    private static boolean isRunningOnHeroku(){
        return System.getenv("HEROKU") != null && Boolean.parseBoolean(System.getenv("HEROKU"));
    }
}
