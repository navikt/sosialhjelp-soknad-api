package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisasjonConsumerMock {

    private static Map<String, OrganisasjonNoekkelinfoDto> responses = new HashMap<>();

    private static final String orgnrMock = "123457890";

    public OrganisasjonConsumer organisasjonConsumerMock() {
        OrganisasjonConsumer mock = mock(OrganisasjonConsumer.class);

        when(mock.hentOrganisasjonNoekkelinfo(anyString()))
                .thenAnswer(OrganisasjonConsumerMock::getDefaultOrganisasjonResponse);

        return mock;
    }

    private static OrganisasjonNoekkelinfoDto getDefaultOrganisasjonResponse(InvocationOnMock invocationOnMock) {
        OrganisasjonNoekkelinfoDto response = responses.get(OidcFeatureToggleUtils.getUserId());
        if (response == null) {
            response = getDefaultNoekkelinfo();
            responses.put(OidcFeatureToggleUtils.getUserId(), response);
        }
        return response;
    }

    private static OrganisasjonNoekkelinfoDto getDefaultNoekkelinfo() {
        return new OrganisasjonNoekkelinfoDto(
                new NavnDto("NAV MOCK AS", null, null, null, null),
                orgnrMock);
    }

    public static void setOrganisasjon(String jsonOrganisasjon) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OrganisasjonNoekkelinfoDto response = mapper.readValue(jsonOrganisasjon, OrganisasjonNoekkelinfoDto.class);

            if (responses.get(OidcFeatureToggleUtils.getUserId()) == null){
                responses.put(OidcFeatureToggleUtils.getUserId(), response);
            } else {
                responses.replace(OidcFeatureToggleUtils.getUserId(), response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetOrganisasjon() {
        responses.replace(OidcFeatureToggleUtils.getUserId(), getDefaultNoekkelinfo());
    }
}
