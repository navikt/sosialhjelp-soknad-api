package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler;

public class OidcFeatureToggleUtils {

    public final static String IS_RUNNING_WITH_OIDC = "authentication.isRunningWithOidc";

    public static boolean isRunningWithOidc(){
        return Boolean.valueOf(System.getProperty(IS_RUNNING_WITH_OIDC,"false"));
    }

    public static String getUserId() {
        if (isRunningWithOidc()) {
            return SubjectHandler.getUserIdFromToken();
        }
        return SamlSubjectHandler.getSubjectHandler().getUid();
    }

    public static String getToken() {
        if (isRunningWithOidc()) {
            return SubjectHandler.getToken();
        }
        return SamlSubjectHandler.getSubjectHandler().getEksternSsoToken();
    }

    public static String getConsumerId() {
        String consumerId;
        if (isRunningWithOidc()) {
            consumerId = SubjectHandler.getConsumerId();
        } else {
            consumerId = SamlSubjectHandler.getSubjectHandler().getConsumerId();
        }
        return consumerId != null? consumerId : "srvsoknadsosialhje";
    }
}
