package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.*;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdConsumerMock {

    private static Map<String, List<ArbeidsforholdDto>> responses = new HashMap<>();

    public ArbeidsforholdConsumer arbeidsforholdConsumerMock() {

        ArbeidsforholdConsumer mock = mock(ArbeidsforholdConsumer.class);

        when(mock.finnArbeidsforholdForArbeidstaker(anyString()))
                .thenAnswer(ArbeidsforholdConsumerMock::getOrCreateCurrentUserResponse);

        return mock;
    }

    public static List<ArbeidsforholdDto> getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock) {
        List<ArbeidsforholdDto> response = responses.get(OidcFeatureToggleUtils.getUserId());
        if (response == null) {
            response = singletonList(defaultArbeidsforhold());
            responses.put(OidcFeatureToggleUtils.getUserId(), response);
        }

        return response;
    }

    public static void setArbeidsforhold(String arbeidsforholdData) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .addMixIn(OpplysningspliktigArbeidsgiverDto.class, OpplysningspliktigArbeidsgiverDtoMixIn.class);

            List<ArbeidsforholdDto> response = mapper.readValue(arbeidsforholdData, new TypeReference<List<ArbeidsforholdDto>>() {
            });

            if (responses.get(OidcFeatureToggleUtils.getUserId()) == null) {
                responses.put(OidcFeatureToggleUtils.getUserId(), response);
            } else {
                responses.replace(OidcFeatureToggleUtils.getUserId(), response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArbeidsforholdDto defaultArbeidsforhold() {
        return new ArbeidsforholdDto.Builder()
                .withAnsettelsesperiode(new AnsettelsesperiodeDto.Builder()
                        .withPeriode(new PeriodeDto.Builder()
                                .withFom(LocalDate.now().minusMonths(3))
                                .withTom(LocalDate.now())
                                .build())
                        .build())
                .withArbeidsavtaler(singletonList(
                        new ArbeidsavtaleDto.Builder()
                                .withStillingsprosent(100.0)
                                .build()))
                .withArbeidsforholdId("id")
                .withArbeidsgiver(new OrganisasjonDto.Builder()
                        .withOrganisasjonsnummer("orgnr")
                        .build())
                .withArbeidstaker(new PersonDto.Builder()
                        .withOffentligIdent("id")
                        .withAktoerId("aktoerid")
                        .build())
                .withNavArbeidsforholdId(1234L)
                .build();
    }

}
