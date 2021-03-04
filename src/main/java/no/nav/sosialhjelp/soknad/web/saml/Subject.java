package no.nav.sosialhjelp.soknad.web.saml;

/* Originally from common-java-modules (auth) */

import lombok.Value;
import lombok.With;
import no.nav.sbl.util.StringUtils;

import java.util.Optional;

import static java.util.Optional.empty;
import static no.nav.sbl.util.AssertUtils.assertNotNull;
import static no.nav.sbl.util.StringUtils.assertNotNullOrEmpty;

@With
@Value
public class Subject {

    private final String uid;
    private final IdentType identType;
    private final SsoToken ssoToken;

    public Subject(String uid, IdentType identType, SsoToken ssoToken) {
        assertNotNullOrEmpty(uid);
        assertNotNull(identType);
        assertNotNull(ssoToken);

        this.uid = uid;
        this.identType = identType;
        this.ssoToken = ssoToken;
    }

    public Optional<String> getSsoToken(SsoToken.Type type) {
        return ssoToken.getType() == type ? StringUtils.of(ssoToken.getToken()) : empty();
    }
}
