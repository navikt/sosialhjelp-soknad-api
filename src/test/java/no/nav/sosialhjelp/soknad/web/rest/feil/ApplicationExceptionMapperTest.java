//package no.nav.sosialhjelp.soknad.web.rest.feil;
//
//import no.nav.sosialhjelp.soknad.client.exceptions.SikkerhetsBegrensningException;
//import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.exception.EttersendelseSendtForSentException;
//import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;
//import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
//import no.nav.sosialhjelp.soknad.domain.model.exception.UgyldigOpplastingTypeException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import javax.ws.rs.core.Response;
//
//import static no.nav.sosialhjelp.soknad.web.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ExtendWith(MockitoExtension.class)
//class ApplicationExceptionMapperTest {
//
//    ApplicationExceptionMapper mapper = new ApplicationExceptionMapper();
//
//    @Test
//    void skalGi415UnsupportedMediaTypeVedUgyldigOpplastingTypeException() {
//        Response response = mapper.toResponse(new UgyldigOpplastingTypeException("feil", new RuntimeException(), "id"));
//        assertThat(response.getStatus()).isEqualTo(415);
//    }
//
//    @Test
//    void skalGi413RequestTooLongVedOpplastingException() {
//        Response response = mapper.toResponse(new OpplastingException("feil", new RuntimeException(), "id"));
//        assertThat(response.getStatus()).isEqualTo(413);
//    }
//
//    @Test
//    void skalGi403ForbiddenVedAuthorizationException() {
//        Response response = mapper.toResponse(new AuthorizationException("feil"));
//        assertThat(response.getStatus()).isEqualTo(403);
//    }
//
//    @Test
//    void skalGi500MedHeaderForIngenBigIpRedirectForAndreKjenteUnntak() {
//        Response response = mapper.toResponse(new SosialhjelpSoknadApiException("feil"));
//        assertThat(response.getStatus()).isEqualTo(500);
//        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true");
//    }
//
//    @Test
//    void skalGi500MedHeaderForIngenBigIpRedirectVedTjenesteUtilgjengeligException() {
//        Response response = mapper.toResponse(new TjenesteUtilgjengeligException("feil", new RuntimeException()));
//        assertThat(response.getStatus()).isEqualTo(500);
//        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true");
//    }
//
//    @Test
//    void skalGi500MedHeaderForIngenBigIpRedirectVedEttersendelseSendtForSentException() {
//        Response response = mapper.toResponse(new EttersendelseSendtForSentException("feil"));
//        assertThat(response.getStatus()).isEqualTo(500);
//        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true");
//    }
//
//    @Test
//    void skalGi500VMedHeaderForIngenBigIpRedirectedSikkerhetsBegrensningException() {
//        Response response = mapper.toResponse(new SikkerhetsBegrensningException("feil", new RuntimeException()));
//        assertThat(response.getStatus()).isEqualTo(500);
//        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true");
//    }
//}
