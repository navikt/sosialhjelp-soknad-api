package no.nav.sbl.dialogarena.rest.feil;

import no.nav.sbl.sosialhjelp.SamtidigOppdateringException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ThrowableMapperTest {

    final private ThrowableMapper mapper = new ThrowableMapper();

    @Test
    public void skalGi409ConflictVedSamtidigOppdateringException() {
        Response response = mapper.toResponse(new SamtidigOppdateringException("Mulig versjonskonflikt..."));
        assertThat(response.getStatus()).isEqualTo(409);
    }
}
