package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LandServiceTest {

    @Mock
    private Kodeverk kodeverk;

    @InjectMocks
    LandService service = new LandService();
    
    @Test
    public void resultatetSkalInneholdeNorgeSelvOmNorgeIkkeKomFraKodeverk() throws Exception {
        when(kodeverk.getLand("SE")).thenReturn("Sverige");
        when(kodeverk.getLand("IS")).thenReturn("Island");
        when(kodeverk.getLand("PL")).thenReturn("Polen");

        List<Land> land = service.hentLand(null);
        assertThat(land.get(0).getText(), equalTo("Norge"));
    }
}
