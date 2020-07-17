package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;

public class OidcFeatureToggleUtils {

    public final static String IS_RUNNING_WITH_OIDC = "authentication.isRunningWithOidc";

    public static boolean isRunningWithOidc(){
        return Boolean.parseBoolean(System.getProperty(IS_RUNNING_WITH_OIDC,"true"));
    }

    public static String getUserId() {
        //if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getIdent().orElseThrow(() -> new AuthorizationException("Missing userId"));
        //}
        //return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getUid();
    }

    public static String getToken() {
        return SubjectHandler.getSsoToken().orElseThrow(() -> new AuthorizationException("Missing token")).getToken();
        /*if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            return SubjectHandler.getToken();
        }
        return no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getEksternSsoToken();
         */
    }

    public static String getConsumerId() {
        /*String consumerId;
        if (isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            consumerId = SubjectHandler.getConsumerId();
        } else {
            consumerId = no.nav.modig.core.context.SubjectHandler.getSubjectHandler().getConsumerId();
        }
        return consumerId != null ? consumerId : "srvsoknadsosialhje";*/
        return "srvsoknadsosialhje";
    }
}
