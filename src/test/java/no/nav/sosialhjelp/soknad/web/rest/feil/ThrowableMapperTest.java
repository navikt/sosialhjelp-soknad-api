//package no.nav.sosialhjelp.soknad.web.rest.feil;
//
//import no.nav.sosialhjelp.soknad.business.exceptions.SamtidigOppdateringException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import javax.ws.rs.core.Response;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ExtendWith(MockitoExtension.class)
//class ThrowableMapperTest {
//
//    final private ThrowableMapper mapper = new ThrowableMapper();
//
//    @Test
//    void skalGi409ConflictVedSamtidigOppdateringException() {
//        Response response = mapper.toResponse(new SamtidigOppdateringException("Mulig versjonskonflikt..."));
//        assertThat(response.getStatus()).isEqualTo(409);
//    }
//}
