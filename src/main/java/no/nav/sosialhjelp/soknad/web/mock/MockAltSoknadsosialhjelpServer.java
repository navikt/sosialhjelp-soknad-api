//package no.nav.sosialhjelp.soknad.web.mock;
//
//import no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer;
//
//import java.io.File;
//import java.util.Objects;
//
//public class MockAltSoknadsosialhjelpServer {
//
//    private static final int PORT = isRunningOnGCP() ? Integer.parseInt(System.getenv("PORT")) : 8181;
//
//    public static void main(String[] args) throws Exception {
//        System.setProperty("mockAltProfil", "true");
//        System.setProperty("logback.configurationFile", "logback-mock-alt.xml");
//
//        File override = new File(Objects.requireNonNull(MockAltSoknadsosialhjelpServer.class.getClassLoader().getResource("override-web-mock-alt.xml")).getFile());
//        SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, override, "/sosialhjelp/soknad-api");
//
//        System.setProperty("NAIS_NAMESPACE", "sosialhjelp-soknad-api-mock-alt");
//
//        server.start();
//    }
//
//    private static boolean isRunningOnGCP(){
//        return System.getenv("GCP") != null && Boolean.parseBoolean(System.getenv("GCP"));
//    }
//}
