package no.nav.sosialhjelp.soknad.web.integration;

import no.nav.sosialhjelp.soknad.web.config.IntegrationConfig;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;

import static no.nav.sosialhjelp.soknad.mock.person.PersonV3Mock.createPersonV3HentPersonRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EndpointDataMocking {

    public static void setupMockWsEndpointData() {
        mockPersonV3Endpoint();
    }

    static void mockPersonV3Endpoint() {
        PersonV3 mock = IntegrationConfig.getMocked("personV3Endpoint");

        try {
            when(mock.hentPerson(any(HentPersonRequest.class))).thenReturn(createPersonV3HentPersonRequest("12"));
        } catch (HentPersonPersonIkkeFunnet | HentPersonSikkerhetsbegrensning hentPersonPersonIkkeFunnet) {
            hentPersonPersonIkkeFunnet.printStackTrace();
        }

    }
}