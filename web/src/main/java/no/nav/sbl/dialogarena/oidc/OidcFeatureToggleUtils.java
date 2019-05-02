package no.nav.sbl.dialogarena.oidc;

public class OidcFeatureToggleUtils {

    public static boolean isRunningWithOidc(){
        return "true".equals(System.getProperty("authentication.isRunningWithOidc","false").toLowerCase());
    }
}
