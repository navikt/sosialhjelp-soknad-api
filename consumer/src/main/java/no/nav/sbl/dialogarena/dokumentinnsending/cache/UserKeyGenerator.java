package no.nav.sbl.dialogarena.dokumentinnsending.cache;

import no.nav.modig.core.context.SubjectHandler;

/**
 * Bean for generating a key for a user given a composite id.
 */
public class UserKeyGenerator {
    public static String generate(String id) {
        return SubjectHandler.getSubjectHandler().getUid() + id;
    }

    public static String generate(Long id) {
        return SubjectHandler.getSubjectHandler().getUid() + id;
    }
}
