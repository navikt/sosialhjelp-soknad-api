package no.nav.sosialhjelp.soknad.web.saml;

import no.nav.common.auth.SubjectHandler;

import java.util.Optional;

public final class SamlSubjectHandler {

    private SamlSubjectHandler() {
    }

    public static String getUserId() {
        Optional<String> ident = SubjectHandler.getIdent();

        return ident.orElse(null);
    }
}
