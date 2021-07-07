package no.nav.sosialhjelp.soknad.web.saml;

import no.nav.sosialhjelp.soknad.domain.model.exception.SamlUnauthorizedException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenAMUserInfoServiceTest {

    @Test(expected = SamlUnauthorizedException.class)
    public void openAmAttributesToMap_withNull_shouldThrowException() {
        OpenAMUserInfoService.openAmAttributesToMap(null);
    }

    @Test
    public void openAmAttributesToMap_withEmptyOpenAmAttribute_shouldNotThrowException() {
        Map<String, String> attributeMap = OpenAMUserInfoService.openAmAttributesToMap(new OpenAMUserInfoService.OpenAMAttributes());
        assertThat(attributeMap).isEqualTo(new HashMap<>());
    }
}
