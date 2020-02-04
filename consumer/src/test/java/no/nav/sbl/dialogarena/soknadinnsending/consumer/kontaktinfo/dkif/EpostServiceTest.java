package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif;

import no.nav.sbl.dialogarena.sendsoknad.domain.DigitalKontaktinfo;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith(value = MockitoJUnitRunner.class)
public class EpostServiceTest {
    private static final String FNR = "12345612345";
    private static final String EPOST = "test@test.no";
    private static final String MOBILNUMMER = "98765432";

    @InjectMocks
    private EpostService epostService;

    @Mock
    private DigitalKontaktinformasjonV1 dkif;

    @Test
    public void mapResponsTilKontaktInfoMapperResponsRiktig() {
        WSHentDigitalKontaktinformasjonResponse response = new WSHentDigitalKontaktinformasjonResponse()
                .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                        .withEpostadresse(new WSEpostadresse().withValue(EPOST))
                        .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(MOBILNUMMER)));

        DigitalKontaktinfo digitalKontaktinfo = epostService.mapResponsTilKontaktInfo(response);

        assertThat(digitalKontaktinfo.getEpostadresse(), is(EPOST));
        assertThat(digitalKontaktinfo.getMobilnummer(), is(MOBILNUMMER));
    }

    @Test
    public void returnererTomEpostHvisDKIFErNede() throws HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        when(dkif.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class))).thenThrow(new HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet());

        DigitalKontaktinfo digitalKontaktinfo = epostService.hentInfoFraDKIF(FNR);

        assertThat(digitalKontaktinfo.getEpostadresse(), isEmptyString());
        assertThat(digitalKontaktinfo.getMobilnummer(), isEmptyString());
    }
}
