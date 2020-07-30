package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;

public class OidcFeatureToggleUtils {

    public final static String IS_RUNNING_WITH_OIDC = "authentication.isRunningWithOidc";

    public static String getToken() {
        return SubjectHandler.getSsoToken().orElseThrow(() -> new AuthorizationException("Missing token")).getToken();
    }

    public static String getConsumerId() {
        return "srvsoknadsosialhje";
    }
}
