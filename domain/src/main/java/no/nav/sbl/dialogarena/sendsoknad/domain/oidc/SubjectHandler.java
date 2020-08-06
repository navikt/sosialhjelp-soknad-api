package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.common.auth.SsoToken;

public interface SubjectHandler {

    String getIdent();

    int getSecurityLevel();

    SsoToken.Type getTokenType();

    boolean subjectIsPresent();

    boolean isOidcTokenPresent();

    String getOIDCTokenAsString();

}
