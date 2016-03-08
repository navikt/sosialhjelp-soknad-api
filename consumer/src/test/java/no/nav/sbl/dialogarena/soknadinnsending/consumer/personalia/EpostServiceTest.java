package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith(value = MockitoJUnitRunner.class)
public class EpostServiceTest {

    @InjectMocks
    private EpostService epostService;


    @Mock
    private DigitalKontaktinformasjonV1 dkif;

    @Test
    public void returnererTomEpostHvisDKIFErNede() throws HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        String fnr = "12345612345";
        when(dkif.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class))).thenThrow(new HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet());

        WSHentDigitalKontaktinformasjonResponse response = epostService.hentInfoFraDKIF(fnr);
        WSKontaktinformasjon kontaktinformasjon = response.getDigitalKontaktinformasjon();

        assertThat(kontaktinformasjon.getEpostadresse(), is(nullValue()) );
    }

}
