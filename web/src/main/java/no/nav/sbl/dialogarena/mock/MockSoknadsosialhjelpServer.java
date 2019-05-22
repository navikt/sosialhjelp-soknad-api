package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

import java.io.File;
import java.util.Objects;

import static no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer.isRunningOnHeroku;

public class MockSoknadsosialhjelpServer {

    public static final int PORT = isRunningOnHeroku() ? Integer.parseInt(System.getenv("PORT")) : 8181;

    public static void main(String[] args) throws Exception {
        System.setProperty("tillatMockRessurs", "true");
        if (!isRunningOnHeroku()) {
            System.setProperty("sendsoknad.datadir", System.getProperty("user.home") + "/kodeverk/sendsoknad");
        }

        File override = new File(Objects.requireNonNull(MockSoknadsosialhjelpServer.class.getClassLoader().getResource("override-web-mock.xml")).getFile());
        SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, override, "/soknadsosialhjelp-server", null);
        server.start();
    }
}
