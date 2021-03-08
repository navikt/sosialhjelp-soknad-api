package no.nav.sosialhjelp.soknad.web.saml;

/* Originally from common-java-modules (auth) */

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static no.nav.sosialhjelp.soknad.web.saml.SsoToken.Type.EKSTERN_OPENAM;
import static no.nav.sosialhjelp.soknad.web.saml.SsoToken.Type.OIDC;
import static no.nav.sosialhjelp.soknad.web.saml.SsoToken.Type.SAML;
import static org.apache.http.util.Asserts.notEmpty;
import static org.apache.http.util.Asserts.notNull;

@Getter
@EqualsAndHashCode
public class SsoToken {
    private final Type type;
    private final String token;
    private final Map<String, Object> attributes;

    SsoToken(Type type, String token, Map<String, ?> attributes) {
        notNull(type, "SsoToken.type");
        notEmpty(token, "SsoToken.token");
        notNull(attributes, "SsoToken.attributes");

        this.type = type;
        this.token = token;
        this.attributes = unmodifiableMap(attributes);
    }

    public static SsoToken oidcToken(String token, Map<String, ?> attributes) {
        return new SsoToken(OIDC, token, attributes);
    }

    public static SsoToken saml(String samlAssertion, Map<String, ?> attributes) {
        return new SsoToken(SAML, samlAssertion, attributes);
    }

    public static SsoToken eksternOpenAM(String token, Map<String, ?> attributes) {
        return new SsoToken(EKSTERN_OPENAM, token, attributes);
    }

    public enum Type {
        OIDC,
        EKSTERN_OPENAM,
        SAML
    }

    @Override
    public String toString() {
        return type.toString();
    }
}