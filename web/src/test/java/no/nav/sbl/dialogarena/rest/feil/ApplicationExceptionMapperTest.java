package no.nav.sbl.dialogarena.rest.feil;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static no.nav.sbl.dialogarena.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationExceptionMapperTest {

    ApplicationExceptionMapper mapper = new ApplicationExceptionMapper();

    @Test
    public void skalGi415UnsupportedMediaTypeVedUgyldigOpplastingTypeException() {
        Response response = mapper.toResponse(new UgyldigOpplastingTypeException("feil", new RuntimeException(), "id"));
        assertThat(response.getStatus()).isEqualTo(415);
    }

    @Test
    public void skalGi413RequestTooLongVedOpplastingException() {
        Response response = mapper.toResponse(new OpplastingException("feil", new RuntimeException(), "id"));
        assertThat(response.getStatus()).isEqualTo(413);
    }

    @Test
    public void skalGi403ForbiddenVedAuthorizationException() {
        Response response = mapper.toResponse(new AuthorizationException("feil"));
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    public void skalGi500MedHeaderForIngenBigIpRedirectForAndreKjenteUnntak() {
        Response response = mapper.toResponse(new ApplicationException("feil"));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true");
    }
}
