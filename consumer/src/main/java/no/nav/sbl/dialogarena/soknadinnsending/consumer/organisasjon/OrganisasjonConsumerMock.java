package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.common.auth.SubjectHandler;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisasjonConsumerMock {

    private static Map<String, OrganisasjonNoekkelinfoDto> responses = new HashMap<>();

    private static final String orgnrMock = "123457890";

    public OrganisasjonConsumer organisasjonConsumerMock() {
        OrganisasjonConsumer mock = mock(OrganisasjonConsumer.class);

        when(mock.hentOrganisasjonNoekkelinfo(anyString()))
                .thenAnswer(OrganisasjonConsumerMock::getFromMapOrDefaultOrganisasjonNoekkelinfo);

        return mock;
    }

    private static OrganisasjonNoekkelinfoDto getFromMapOrDefaultOrganisasjonNoekkelinfo(InvocationOnMock invocationOnMock) {
        OrganisasjonNoekkelinfoDto response = responses.get(SubjectHandler.getIdent().orElse(null));
        if (response == null) {
            response = getDefaultOrganisasjonNoekkelinfo();
            responses.put(SubjectHandler.getIdent().orElse(null), response);
        }
        return response;
    }

    private static OrganisasjonNoekkelinfoDto getDefaultOrganisasjonNoekkelinfo() {
        return new OrganisasjonNoekkelinfoDto(
                new NavnDto("NAV MOCK AS", null, null, null, null),
                orgnrMock);
    }

    public static void setOrganisasjon(String jsonOrganisasjon) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OrganisasjonNoekkelinfoDto response = mapper.readValue(jsonOrganisasjon, OrganisasjonNoekkelinfoDto.class);

            if (responses.get(SubjectHandler.getIdent().orElse(null)) == null){
                responses.put(SubjectHandler.getIdent().orElse(null), response);
            } else {
                responses.replace(SubjectHandler.getIdent().orElse(null), response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetOrganisasjon() {
        responses.replace(SubjectHandler.getIdent().orElse(null), getDefaultOrganisasjonNoekkelinfo());
    }
}
