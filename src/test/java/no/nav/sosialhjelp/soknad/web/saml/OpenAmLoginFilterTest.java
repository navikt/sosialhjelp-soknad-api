package no.nav.sosialhjelp.soknad.web.saml;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAmLoginFilterTest {

    @Test
    void metadataPingPath_shouldBeUnprotectedBySAML() {
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/ping")).isFalse();
    }

    @Test
    void metadataOidcPath_shouldBeUnprotectedBySAML() {
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/innsendte")).isFalse();
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/pabegynte")).isFalse();
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/ettersendelse")).isFalse();
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/oidc/ping")).isFalse();
    }

    @Test
    void metadataPaths_shouldBeProtectedBySAML() {
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/innsendte")).isTrue();
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/pabegynte")).isTrue();
        assertThat(OpenAmLoginFilter.isPathProtectedBySAML("/sosialhjelp/soknad-api/metadata/ettersendelse")).isTrue();
    }
}
