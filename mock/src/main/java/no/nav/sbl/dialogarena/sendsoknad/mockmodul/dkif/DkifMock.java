package no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DkifMock {

    private static final String RIKTIG_IDENT = "***REMOVED***";
    private static final String EN_EPOST = "test@epost.com";
    private static final String ET_TELEFONNUMMER = "98765432";

    public DigitalKontaktinformasjon_v1PortType dkifMock(){

        DigitalKontaktinformasjon_v1PortType mock = mock(DigitalKontaktinformasjon_v1PortType.class);
        WSHentDigitalKontaktinformasjonResponse response = new WSHentDigitalKontaktinformasjonResponse();

        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon()
                .withPersonident(RIKTIG_IDENT)
                .withEpostadresse(new WSEpostadresse().withValue(EN_EPOST))
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(ET_TELEFONNUMMER))
                .withReservasjon("TEST");

        response.setDigitalKontaktinformasjon(kontaktinformasjon);

        try{
            when(mock.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class))).thenReturn(response);
        }catch(HentDigitalKontaktinformasjonPersonIkkeFunnet | HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet | HentDigitalKontaktinformasjonSikkerhetsbegrensing e ) {
            throw new RuntimeException(e);
        }
        return mock;
    }
}
