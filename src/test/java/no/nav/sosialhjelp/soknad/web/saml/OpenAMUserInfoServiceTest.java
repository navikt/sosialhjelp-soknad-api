package no.nav.sosialhjelp.soknad.web.saml;

import no.nav.sosialhjelp.soknad.domain.model.exception.SamlUnauthorizedException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class OpenAMUserInfoServiceTest {

    @Test
    public void openAmAttributesToMap_withNull_shouldThrowException() {
        assertThatExceptionOfType(SamlUnauthorizedException.class)
                .isThrownBy(() -> OpenAMUserInfoService.openAmAttributesToMap(null));
    }

    @Test
    public void openAmAttributesToMap_withEmptyOpenAmAttribute_shouldNotThrowException() {
        Map<String, String> attributeMap = OpenAMUserInfoService.openAmAttributesToMap(new OpenAMUserInfoService.OpenAMAttributes());
        assertThat(attributeMap).isEqualTo(new HashMap<>());
    }
}
