package no.nav.sbl.dialogarena.saml;

import no.nav.common.auth.SubjectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SamlSubjectHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SamlSubjectHandler.class);

    public static String getUserId() {
        Optional<String> ident = SubjectHandler.getIdent();

        if (ident.isPresent()) {
            log.info("DEBUG SAML SubjectHandler.getSubject: 0 no.nav.common.auth.SubjectHandler.ident " + ident.get());
            return ident.get();
        }
        log.warn("Finner ingen SAML-ident i request");
        return null;
    }
}
