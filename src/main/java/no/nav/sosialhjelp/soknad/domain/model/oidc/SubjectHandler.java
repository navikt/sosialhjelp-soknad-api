//package no.nav.sosialhjelp.soknad.domain.model.oidc;
//
//import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public final class SubjectHandler {
//
//    private SubjectHandler() {
//    }
//
//    private static final Logger logger = LoggerFactory.getLogger(SubjectHandler.class);
//
//    private static SubjectHandlerService subjectHandlerService = new OidcSubjectHandlerService();
//
//
//    public static String getUserId() {
//        return subjectHandlerService.getUserIdFromToken();
//    }
//
//    public static String getToken() {
//        return subjectHandlerService.getToken();
//    }
//
//    public static String getConsumerId() {
//        return subjectHandlerService.getConsumerId();
//    }
//
//    public static SubjectHandlerService getSubjectHandlerService() {
//        return subjectHandlerService;
//    }
//
//    public static void setSubjectHandlerService(SubjectHandlerService subjectHandlerServiceImpl) {
//        if (ServiceUtils.isNonProduction()) {
//            subjectHandlerService = subjectHandlerServiceImpl;
//        } else {
//            logger.error("Forsøker å sette en annen SubjectHandlerService i prod!");
//            throw new RuntimeException("Forsøker å sette en annen SubjectHandlerService i prod!");
//        }
//    }
//
//    public static void resetOidcSubjectHandlerService() {
//        subjectHandlerService = new OidcSubjectHandlerService();
//    }
//}
