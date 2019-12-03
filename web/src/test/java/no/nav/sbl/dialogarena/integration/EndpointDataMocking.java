package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import org.mockito.Mockito;

import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonV3Mock.createPersonV3HentPersonRequest;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EndpointDataMocking {

    private static int behandlingsIdCounter = 1;

    public static void setupMockWsEndpointData() throws Exception {
        mockPersonV3Endpoint();
        mockDkifService();
        mockArbeidsForholdService();
    }

    static void mockPersonV3Endpoint() throws Exception {
        PersonV3 mock = IntegrationConfig.getMocked("personV3Endpoint");

        try {
            when(mock.hentPerson(any(HentPersonRequest.class))).thenReturn(createPersonV3HentPersonRequest("12"));
        } catch (HentPersonPersonIkkeFunnet | HentPersonSikkerhetsbegrensning hentPersonPersonIkkeFunnet) {
            hentPersonPersonIkkeFunnet.printStackTrace();
        }

    }

    static void mockDkifService() throws Exception {
        DigitalKontaktinformasjonV1 dkif = IntegrationConfig.getMocked("dkifService");
        Mockito.when(dkif.hentDigitalKontaktinformasjon(any())).thenReturn(
                new WSHentDigitalKontaktinformasjonResponse()
                        .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                                .withEpostadresse(new WSEpostadresse().withValue(""))
                        )
        );
    }

    static void mockArbeidsForholdService() throws Exception {
        ArbeidsforholdV3 arbeidEndpont = IntegrationConfig.getMocked("arbeidEndpoint");
        Mockito.when(arbeidEndpont.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class)))
                .thenReturn(new FinnArbeidsforholdPrArbeidstakerResponse());
    }
}