package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisasjonConsumerMock {

    public OrganisasjonConsumer organisasjonConsumerMock(){
        OrganisasjonConsumer mock = mock(OrganisasjonConsumer.class);

        when(mock.hentOrganisasjonNoekkelinfo(any(String.class)))
                .thenAnswer(OrganisasjonConsumerMock::getOrCreateOrganisasjonResponse);

        return mock;
    }


    private static OrganisasjonNoekkelinfoDto getOrCreateOrganisasjonResponse(InvocationOnMock invocationOnMock) {
        // todo implement
        return null;
    }
}
