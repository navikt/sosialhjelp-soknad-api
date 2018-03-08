package no.nav.sbl.dialogarena;

import static java.lang.System.setProperty;
import static no.nav.modig.test.util.FilesAndDirs.TEST_RESOURCES;

import java.io.File;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;

public class DevSoknadsosialhjelpServer {
    
    public static final int PORT = 8181;
    

    public static void main(String[] args) throws Exception {
        final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, new File(TEST_RESOURCES, "override-web.xml"), "/sendsoknad", null);
        setProperty(StaticSubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        server.start();
    }
    
}
