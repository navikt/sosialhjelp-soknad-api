package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.common.auth.SsoToken;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
public class DefaultSubjectHandler implements SubjectHandler {

    public String getUserId() {
        return no.nav.common.auth.SubjectHandler.getIdent().orElse(null); //.orElseThrow(() -> new AuthorizationException("Ingen innlogget bruker"));
    }

    public int getSecurityLevel() {
        return 0;
    }

    public SsoToken.Type getTokenType() {
        return null;
    }

    public boolean subjectIsPresent() {
        return no.nav.common.auth.SubjectHandler.getSubject().isPresent();    }

    public boolean isOidcTokenPresent() {
        return false;
    }

    public String getOIDCTokenAsString() {
        return no.nav.common.auth.SubjectHandler.getSsoToken()
                .filter(t -> t.getType().equals(SsoToken.Type.OIDC))
                .orElseThrow(() -> new UnauthorizedException("Fant ikke token for bruker"))
                .getToken();
    }
}
