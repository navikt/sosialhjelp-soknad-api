package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.AnsettelsesperiodeDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.ArbeidsavtaleDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.ArbeidsforholdDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.OrganisasjonDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.PeriodeDto;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.PersonDto;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdConsumerMock {

    private static final Map<String, List<ArbeidsforholdDto>> responses = new HashMap<>();

    public ArbeidsforholdConsumer arbeidsforholdConsumerMock() {

        ArbeidsforholdConsumer mock = mock(ArbeidsforholdConsumer.class);

        when(mock.finnArbeidsforholdForArbeidstaker(anyString()))
                .thenAnswer(ArbeidsforholdConsumerMock::getOrCreateCurrentUserResponse);

        return mock;
    }

    public static List<ArbeidsforholdDto> getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock) {
        List<ArbeidsforholdDto> response = responses.get(SubjectHandler.getUserId());
        if (response == null) {
            response = singletonList(defaultArbeidsforhold());
            responses.put(SubjectHandler.getUserId(), response);
        }

        return response;
    }

    public static void setArbeidsforhold(String arbeidsforholdData) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .registerModule(new JavaTimeModule());

            List<ArbeidsforholdDto> response = mapper.readValue(arbeidsforholdData, new TypeReference<List<ArbeidsforholdDto>>() {
            });

            if (responses.get(SubjectHandler.getUserId()) == null) {
                responses.put(SubjectHandler.getUserId(), response);
            } else {
                responses.replace(SubjectHandler.getUserId(), response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArbeidsforholdDto defaultArbeidsforhold() {
        AnsettelsesperiodeDto ansettelsesperiode = new AnsettelsesperiodeDto(new PeriodeDto(LocalDate.now().minusMonths(3), LocalDate.now()));
        ArbeidsavtaleDto arbeidsavtale = new ArbeidsavtaleDto(100.0);
        OrganisasjonDto arbeidsgiver = new OrganisasjonDto("orgnr", "Organisasjon");
        PersonDto arbeidstaker = new PersonDto("id", "aktoerId", "Person");
        return new ArbeidsforholdDto(
                ansettelsesperiode,
                singletonList(arbeidsavtale),
                "id",
                arbeidsgiver,
                arbeidstaker,
                1234L);
    }

}
