package no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
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

    private static final String RIKTIG_IDENT = "***REMOVED***";
    private static final String EN_EPOST = "test@epost.com";

    private static Map<String, WSHentDigitalKontaktinformasjonResponse> responses = new HashMap<>();

    public DigitalKontaktinformasjonV1 dkifMock(){

        final DigitalKontaktinformasjonV1 mock = mock(DigitalKontaktinformasjonV1.class);

        try{
            when(mock.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                    .thenAnswer((invocationOnMock) -> getOrCreateCurrentUserResponse());
        } catch(HentDigitalKontaktinformasjonPersonIkkeFunnet | HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet | HentDigitalKontaktinformasjonSikkerhetsbegrensing e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

    private static WSHentDigitalKontaktinformasjonResponse getOrCreateCurrentUserResponse() {

        WSHentDigitalKontaktinformasjonResponse response = responses.get(SubjectHandler.getSubjectHandler().getUid());
        if (response == null) {
            response = createNewResponse();
            responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
        }
        return response;
    }

    private static WSHentDigitalKontaktinformasjonResponse createNewResponse(){
        final WSHentDigitalKontaktinformasjonResponse response = new WSHentDigitalKontaktinformasjonResponse();
        final WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon();
        response.setDigitalKontaktinformasjon(kontaktinformasjon);
        return response;
    }

    public static void setTelefonnummer(String telefonnummer) {

        WSHentDigitalKontaktinformasjonResponse response = getOrCreateCurrentUserResponse();

        if (!telefonnummer.isEmpty() && telefonnummer.matches("^\\d{8}")) {
            response.getDigitalKontaktinformasjon().withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(telefonnummer));
        } else if (telefonnummer.equals("slett")){
            response.getDigitalKontaktinformasjon().setMobiltelefonnummer(null);
        }
    }
}
