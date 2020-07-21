package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.common.auth.SsoToken;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
public class DefaultSubjectHandlerWrapper implements SubjectHandlerWrapper {

    public String getIdent() {
        return SubjectHandler.getIdent().orElseThrow(() -> new UnauthorizedException("Ingen innlogget bruker"));
    }

    public int getSecurityLevel() {
        return 0;
    }

    public SsoToken.Type getTokenType() {
        return null;
    }

    public boolean subjectIsPresent() {
        return SubjectHandler.getSubject().isPresent();    }

    public boolean isOidcTokenPresent() {
        return false;
    }

    public String getOIDCTokenAsString() {
        return SubjectHandler.getSsoToken()
                .filter(t -> t.getType().equals(SsoToken.Type.OIDC))
                .orElseThrow(() -> new UnauthorizedException("Fant ikke token for bruker"))
                .getToken();
    }
}
