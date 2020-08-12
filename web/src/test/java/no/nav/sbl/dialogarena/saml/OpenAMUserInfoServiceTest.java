package no.nav.sbl.dialogarena.saml;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SamlUnauthorizedException;
import org.junit.Test;

public class OpenAMUserInfoServiceTest {

    @Test(expected = SamlUnauthorizedException.class)
    public void openAmAttributesToMap_withNull_shouldThrowException() {
        OpenAMUserInfoService.openAmAttributesToMap(null);
    }

    @Test
    public void openAmAttributesToMap_withEmptyOpenAmAttribute_shouldThrowException() {
        OpenAMUserInfoService.openAmAttributesToMap(new OpenAMUserInfoService.OpenAMAttributes());
    }

}