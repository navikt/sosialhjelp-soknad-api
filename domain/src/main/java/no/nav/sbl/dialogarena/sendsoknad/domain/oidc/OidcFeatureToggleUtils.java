package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

public class OidcFeatureToggleUtils {

    public final static String IS_RUNNING_WITH_OIDC = "authentication.isRunningWithOidc";

    public static boolean isRunningWithOidc(){
        return Boolean.valueOf(System.getProperty(IS_RUNNING_WITH_OIDC,"false"));
    }

    public static String getUserId() {
        if (isRunningWithOidc()) {
            return SubjectHandler.getUserIdFromToken();
        }
        return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getUid();
    }

    public static String getToken() {
        if (isRunningWithOidc()) {
            return SubjectHandler.getToken();
        }
        return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getEksternSsoToken();
    }

    public static String getConsumerId() {
        String consumerId;
        if (isRunningWithOidc()) {
            consumerId = SubjectHandler.getConsumerId();
        } else {
            consumerId = no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getConsumerId();
        }
        return consumerId != null? consumerId : "srvsoknadsosialhje";
    }
}
