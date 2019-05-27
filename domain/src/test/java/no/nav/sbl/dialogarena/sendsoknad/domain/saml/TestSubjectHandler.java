package no.nav.sbl.dialogarena.sendsoknad.domain.saml;

import javax.security.auth.Subject;

public abstract class TestSubjectHandler extends SamlSubjectHandler {

    public abstract void setSubject(Subject subject);

    public abstract void reset();
}
