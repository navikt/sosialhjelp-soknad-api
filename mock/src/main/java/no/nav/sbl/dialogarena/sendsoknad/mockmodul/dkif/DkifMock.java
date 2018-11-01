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
    private static Map<String, String> telefonnumre = new HashMap<>();
    static {
        telefonnumre.put(null, "98765432");
    }

    public DigitalKontaktinformasjonV1 dkifMock(){
        final DigitalKontaktinformasjonV1 mock = mock(DigitalKontaktinformasjonV1.class);

        try{
            when(mock.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                    .thenAnswer((invocationOnMock) -> generateResponse());
        } catch(HentDigitalKontaktinformasjonPersonIkkeFunnet | HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet | HentDigitalKontaktinformasjonSikkerhetsbegrensing e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

    private static final WSHentDigitalKontaktinformasjonResponse generateResponse() {
        final WSHentDigitalKontaktinformasjonResponse response = new WSHentDigitalKontaktinformasjonResponse();

        final String telefonnummer = telefonnumre.get(SubjectHandler.getSubjectHandler().getConsumerId());
        final WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon()
                .withPersonident(RIKTIG_IDENT)
                .withEpostadresse(new WSEpostadresse().withValue(EN_EPOST))
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(telefonnummer))
                .withReservasjon("TEST");

        response.setDigitalKontaktinformasjon(kontaktinformasjon);
        return response;
    }

    public static final void setTelefonnummer(String telefonnummer) {
        DkifMock.telefonnumre.put(SubjectHandler.getSubjectHandler().getConsumerId(), telefonnummer);
    }
}
