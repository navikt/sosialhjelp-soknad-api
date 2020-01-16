package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisasjonConsumerMock {

    private static final String orgnrMock = "123457890";

    public OrganisasjonConsumer organisasjonConsumerMock(){
        OrganisasjonConsumer mock = mock(OrganisasjonConsumer.class);

        when(mock.hentOrganisasjonNoekkelinfo(anyString()))
                .thenAnswer(OrganisasjonConsumerMock::getDefaultOrganisasjonResponse);

        return mock;
    }

    private static OrganisasjonNoekkelinfoDto getDefaultOrganisasjonResponse(InvocationOnMock invocationOnMock) {
        return new OrganisasjonNoekkelinfoDto(
                new NavnDto("NAV MOCK AS", null, null, null, null),
                orgnrMock
        );
    }
}
