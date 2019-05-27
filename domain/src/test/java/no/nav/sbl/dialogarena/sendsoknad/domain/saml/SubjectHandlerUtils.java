package no.nav.sbl.dialogarena.sendsoknad.domain.saml;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain.*;

import javax.security.auth.Subject;

/**
 * Utilityclass that provides support for populating and resetting TestSubjectHandlers.
 */
public class SubjectHandlerUtils {

    /**
     * @see TestSubjectHandler#reset()
     */
    public static void reset() {
        ((TestSubjectHandler) SamlSubjectHandler.getSubjectHandler()).reset();
    }

    /**
     * @param openAmToken
     *            - if null it will be generated with a value of "&lt;userId&gt;-&lt;authLevel&gt;", eg "01015245464-4" for the
     *            userId "01015245464" with autLevel "4"
     */
    public static void setEksternBruker(String userId, int authLevel, String openAmToken) {

        setSubject(new SubjectBuilder(userId, IdentType.EksternBruker).withAuthLevel(authLevel).withOpenAmToken(openAmToken).getSubject());
    }

    public static void setInternBruker(String userId) {
        setSubject(new SubjectBuilder(userId, IdentType.InternBruker).getSubject());
    }

    public static void setSystemressurs(String userId) {
        setSubject(new SubjectBuilder(userId, IdentType.Systemressurs).getSubject());
    }

    public static void setSubject(Subject subject) {
        ((TestSubjectHandler) SamlSubjectHandler.getSubjectHandler()).setSubject(subject);
    }

    public static class SubjectBuilder {
        private String userId;
        private IdentType identType;
        private int authLevel;
        private String openAmToken;

        public SubjectBuilder(String userId, IdentType identType) {
            this.userId = userId;
            this.identType = identType;
            if (IdentType.InternBruker.equals(identType)) {
                authLevel = 4;
            }
        }

        public SubjectBuilder withAuthLevel(int authLevel) {
            this.authLevel = authLevel;
            return this;
        }

        public SubjectBuilder withOpenAmToken(String openAmToken) {
            this.openAmToken = openAmToken;
            return this;
        }

        public Subject getSubject() {
            Subject subject = new Subject();
            subject.getPrincipals().add(new SluttBruker(userId, identType));
            subject.getPublicCredentials().add(new AuthenticationLevelCredential(authLevel));
            subject.getPrincipals().add(new ConsumerId());
            if (IdentType.EksternBruker.equals(identType)) {
                subject.getPublicCredentials().add(new OpenAmTokenCredential(openAmToken == null ? userId + "-" + authLevel : openAmToken));
            }
            return subject;
        }
    }
}
