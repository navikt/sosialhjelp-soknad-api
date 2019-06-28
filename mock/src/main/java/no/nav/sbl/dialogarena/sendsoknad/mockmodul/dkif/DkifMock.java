package no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DkifMock {

    private static Map<String, WSHentDigitalKontaktinformasjonResponse> responses = new HashMap<>();

    public DigitalKontaktinformasjonV1 dkifMock(){

        DigitalKontaktinformasjonV1 mock = mock(DigitalKontaktinformasjonV1.class);

        try{
            when(mock.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                    .thenAnswer((invocationOnMock) -> getOrCreateCurrentUserResponse(OidcFeatureToggleUtils.getUserId()));
        } catch(HentDigitalKontaktinformasjonPersonIkkeFunnet | HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet | HentDigitalKontaktinformasjonSikkerhetsbegrensing e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

    private static WSHentDigitalKontaktinformasjonResponse getOrCreateCurrentUserResponse(String fnr) {

        WSHentDigitalKontaktinformasjonResponse response = responses.get(fnr);
        if (response == null) {
            response = createNewResponse();
            responses.put(fnr, response);
        }
        System.out.println(responses);
        return response;
    }

    private static WSHentDigitalKontaktinformasjonResponse createNewResponse(){
        WSHentDigitalKontaktinformasjonResponse response = new WSHentDigitalKontaktinformasjonResponse();
        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon();
        response.setDigitalKontaktinformasjon(kontaktinformasjon);
        return response;
    }

    public static void setTelefonnummer(JsonTelefonnummer telefonnummer, String fnr) {

        WSHentDigitalKontaktinformasjonResponse response = getOrCreateCurrentUserResponse(fnr);
        response
                .getDigitalKontaktinformasjon()
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(telefonnummer.getVerdi()));
    }

    public static void resetTelefonnummer(String fnr) {

        WSHentDigitalKontaktinformasjonResponse response = getOrCreateCurrentUserResponse(fnr);
        response.getDigitalKontaktinformasjon().setMobiltelefonnummer(null);
    }
}
