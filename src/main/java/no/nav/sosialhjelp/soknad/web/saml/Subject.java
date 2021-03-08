package no.nav.sosialhjelp.soknad.web.saml;

/* Originally from common-java-modules (auth) */

import lombok.Value;
import lombok.With;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.apache.http.util.Asserts.notEmpty;
import static org.apache.http.util.Asserts.notNull;

@With
@Value
public class Subject {

    private final String uid;
    private final IdentType identType;
    private final SsoToken ssoToken;

    public Subject(String uid, IdentType identType, SsoToken ssoToken) {
        notEmpty(uid, "Subject.uid");
        notNull(identType, "Subject.identType");
        notNull(ssoToken, "Subject.ssoToken");

        this.uid = uid;
        this.identType = identType;
        this.ssoToken = ssoToken;
    }

    public Optional<String> getSsoToken(SsoToken.Type type) {
        return ssoToken.getType() == type ? Optional.of(ssoToken.getToken()) : empty();
    }
}
