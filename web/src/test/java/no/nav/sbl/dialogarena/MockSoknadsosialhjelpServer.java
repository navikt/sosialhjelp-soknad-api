package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

import java.io.File;

import static no.nav.modig.core.test.FilesAndDirs.TEST_RESOURCES;

public class MockSoknadsosialhjelpServer {

    public static final int PORT = 8181;

    public static void main(String[] args) throws Exception {
        System.setProperty("tillatMockRessurs", "true");

        final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, new File(TEST_RESOURCES, "override-web-mock.xml"), "/soknadsosialhjelp-server", null);
        server.start();
    }

}
