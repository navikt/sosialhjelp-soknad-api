package no.nav.sosialhjelp.soknad.web.integration;

import no.nav.sosialhjelp.soknad.web.config.IntegrationConfig;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;

import static no.nav.sosialhjelp.soknad.mock.person.PersonV3Mock.createPersonV3HentPersonRequestForIntegrationTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EndpointDataMocking {

    private static int behandlingsIdCounter = 1;

    public static void setupMockWsEndpointData() throws Exception {
        mockPersonV3Endpoint();
    }

    static void mockPersonV3Endpoint() throws Exception {
        PersonV3 mock = IntegrationConfig.getMocked("personV3Endpoint");

        try {
            when(mock.hentPerson(any(HentPersonRequest.class))).thenReturn(createPersonV3HentPersonRequestForIntegrationTest("12"));
        } catch (HentPersonPersonIkkeFunnet | HentPersonSikkerhetsbegrensning hentPersonPersonIkkeFunnet) {
            hentPersonPersonIkkeFunnet.printStackTrace();
        }

    }
}