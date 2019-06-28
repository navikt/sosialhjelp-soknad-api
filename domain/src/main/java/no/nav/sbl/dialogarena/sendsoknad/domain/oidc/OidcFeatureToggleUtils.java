package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler;

public class OidcFeatureToggleUtils {

    public final static String IS_RUNNING_WITH_OIDC = "authentication.isRunningWithOidc";

    public static boolean isRunningWithOidc(){
        return Boolean.valueOf(System.getProperty(IS_RUNNING_WITH_OIDC,"false"));
    }

    public static String getUserId() {
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getUserIdFromToken();
        }
        return SamlSubjectHandler.getSubjectHandler().getUid();
    }

    public static String getToken() {
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getToken();
        }
        return SamlSubjectHandler.getSubjectHandler().getEksternSsoToken();
    }

    public static String getConsumerId() {
        String consumerId;
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            consumerId = SubjectHandler.getConsumerId();
        } else {
            consumerId = SamlSubjectHandler.getSubjectHandler().getConsumerId();
        }
        return consumerId != null ? consumerId : "srvsoknadsosialhje";
    }
}
