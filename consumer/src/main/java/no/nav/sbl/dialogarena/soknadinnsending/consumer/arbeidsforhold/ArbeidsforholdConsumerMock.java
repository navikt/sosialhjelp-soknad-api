package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.*;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdConsumerMock {

    private static Map<String, ArbeidsforholdDto> responses = new HashMap<>();

    public ArbeidsforholdConsumer arbeidsforholdConsumerMock() {

        ArbeidsforholdConsumer mock = mock(ArbeidsforholdConsumer.class);

        when(mock.finnArbeidsforholdForArbeidstaker(anyString()))
                .thenAnswer(ArbeidsforholdConsumerMock::getOrCreateCurrentUserResponse);

        return mock;
    }

    public static ArbeidsforholdDto getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock) {
        ArbeidsforholdDto response = responses.get(OidcFeatureToggleUtils.getUserId());
        if (response == null) {
            response = defaultArbeidsforhold();
            responses.put(OidcFeatureToggleUtils.getUserId(), response);
        }

        return response;
    }

    private static ArbeidsforholdDto defaultArbeidsforhold() {
        AnsettelsesperiodeDto ansettelsesperiode = new AnsettelsesperiodeDto(new PeriodeDto(LocalDate.now().minusMonths(3), LocalDate.now()));
        ArbeidsavtaleDto arbeidsavtaleDto = new ArbeidsavtaleDto(0.0, "", 0.0, null, null, "", "", 0.0, "");
        OrganisasjonDto arbeidsgiver = new OrganisasjonDto("orgnr");
        PersonDto arbeidstaker = new PersonDto("id", "aktoerid");
        OrganisasjonDto opplysningspliktig = new OrganisasjonDto("org2");
        return new ArbeidsforholdDto(
                ansettelsesperiode,
                Collections.singletonList(arbeidsavtaleDto),
                "id",
                arbeidsgiver,
                arbeidstaker,
                1234L,
                opplysningspliktig
        );
    }

}
