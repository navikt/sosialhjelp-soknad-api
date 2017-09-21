package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SynligeFaktaServiceTest {

    @Mock
    SoknadService soknadService;

    @InjectMocks
    SynligeFaktaService synligeFaktaService;

    @Before
    public void setOppMock() {
        SoknadStruktur soknadStruktur = new SoknadStruktur();

        FaktumStruktur key1Struktur = new FaktumStruktur().medId("key1");
        key1Struktur.setPanel("panel1");
        key1Struktur.setType("hidden");

        FaktumStruktur key2Struktur = new FaktumStruktur().medId("key2");
        key2Struktur.setPanel("panel2");

        FaktumStruktur spm1Struktur = new FaktumStruktur().medId("key1.spm1").medDependOn(key1Struktur).medDependOnValues(asList("123"));
        spm1Struktur.setPanel("");
        FaktumStruktur spm2Struktur = new FaktumStruktur().medId("key1.spm2").medDependOn(key1Struktur).medDependOnValues(asList("456"));

        soknadStruktur.setFakta(asList(key1Struktur, key2Struktur, spm1Struktur, spm2Struktur));

        when(soknadService.hentSoknadStruktur(anyString())).thenReturn(soknadStruktur);

        when(soknadService.hentSoknad(any(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad()
                .medFaktum(new Faktum().medFaktumId(1L).medKey("key1").medValue("123"))
                .medFaktum(new Faktum().medFaktumId(2L).medKey("key1.spm1").medParrentFaktumId(1L))
                .medFaktum(new Faktum().medFaktumId(3L).medKey("key1.spm2").medParrentFaktumId(1L))
                .medFaktum(new Faktum().medFaktumId(4L).medKey("key2").medValue("ghi"))
        );
    }

    @Test
    public void returnererSynligeFakta() {
        List<FaktumStruktur> synligeFakta = synligeFaktaService.finnSynligeFaktaForSoknad("10000BBB", "panel1");

        assertEquals(1, synligeFakta.size());
        assertEquals("key1.spm1", synligeFakta.get(0).getId());
    }

}