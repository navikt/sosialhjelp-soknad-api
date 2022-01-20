//package no.nav.sosialhjelp.soknad.web;
//
//import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl;
//import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils;
//import no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer;
//
//import java.io.File;
//
//public class DevSoknadsosialhjelpServer {
//
//    public static final int PORT = 8181;
//
//    public static void main(String[] args) throws Exception {
//        SoknadsosialhjelpServer.setFrom("environment-test.properties", false);
//
//        final SoknadsosialhjelpServer server = new SoknadsosialhjelpServer(PORT, new File("src/test/resources/override-web.xml"), "/sosialhjelp/soknad-api");
//        if (isOidcMock()) {
//            SubjectHandlerUtils.INSTANCE.setNewSubjectHandlerImpl(new StaticSubjectHandlerImpl());
//        }
//
//        server.start();
//    }
//
//    private static boolean isOidcMock() {
//        return "true".equalsIgnoreCase(System.getProperty("tillatmock")) &&
//                "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock"));
//    }
//
//}
