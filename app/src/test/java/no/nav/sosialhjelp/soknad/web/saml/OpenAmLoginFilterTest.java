package no.nav.sosialhjelp.soknad.web.saml;

import org.junit.Test;

import static org.junit.Assert.*;

public class OpenAmLoginFilterTest {

    @Test
    public void metadataPingPath_shouldBeUnprotectedBySAML() {
        assertFalse(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/ping"));
    }

    @Test
    public void metadataOidcPath_shouldBeUnprotectedBySAML() {
        assertFalse(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/innsendte"));
        assertFalse(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/pabegynte"));
        assertFalse(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/ettersendelse"));
        assertFalse(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/ping"));
    }

    @Test
    public void metadataPaths_shouldBeProtectedBySAML() {
        assertTrue(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/innsendte"));
        assertTrue(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/pabegynte"));
        assertTrue(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/ettersendelse"));
    }
}
