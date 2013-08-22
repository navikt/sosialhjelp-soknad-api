package no.nav.sbl.dialogarena.dokumentinnsending.security;

import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.core.context.SubjectHandlerUtils;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;

/**
 * Klasse som setter en mock security context for test
 */
public class SecurityHandler {
    public static void setSecurityContext(String userId) {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        System.setProperty(ModigSecurityConstants.SYSTEMUSER_USERNAME, "BD05");
        SubjectHandlerUtils.setEksternBruker(userId, 4, null);
    }
}
