package no.nav.sbl.dialogarena.sts;

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.cxf) */

public enum StsType {
    SYSTEM_USER_IN_FSS,
    ON_BEHALF_OF_WITH_JWT,
    EXTERNAL_SSO_SAML; /* <- should be removed when the application is running with oidc */

    public boolean allowCachingInEndpoint() {
        return this == SYSTEM_USER_IN_FSS;
    }
}
