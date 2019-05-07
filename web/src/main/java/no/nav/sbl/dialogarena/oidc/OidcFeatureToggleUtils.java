package no.nav.sbl.dialogarena.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

public class OidcFeatureToggleUtils {

    public static boolean isRunningWithOidc(){
        return "true".equals(System.getProperty("authentication.isRunningWithOidc","false").toLowerCase());
    }

    public static String getUserId() {
        if (isRunningWithOidc()) {
            return SubjectHandler.getUserIdFromToken();
        }
        return getSubjectHandler().getUid();
    }

    public static String getToken() {
        if (isRunningWithOidc()) {
            return SubjectHandler.getToken();
        }
        return getSubjectHandler().getEksternSsoToken();
    }
}
