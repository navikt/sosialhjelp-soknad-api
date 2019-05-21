package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;

public class OidcFeatureToggleUtils {

    public static boolean isRunningWithOidc(){
        return "true".equals(System.getProperty("authentication.isRunningWithOidc","false").toLowerCase());
    }

    public static String getUserId() {
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getUserIdFromToken();
        }
        return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getUid();
    }

    public static String getToken() {
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getToken();
        }
        return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getEksternSsoToken();
    }

    public static String getConsumerId() {
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getConsumerId();
        }
        return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getConsumerId();
    }
}
