package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdMock {

    public static Map<String, FinnArbeidsforholdPrArbeidstakerResponse> responses = new HashMap<>();

    public ArbeidsforholdV3 arbeidMock() {
        ArbeidsforholdV3 mock = mock(ArbeidsforholdV3.class);

        try {
            when(mock.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class)))
                    .thenAnswer((invocationOnMock -> getOrCreateCurrentUserResponse()));
        } catch (Exception ignored) {
        }
        return mock;
    }

    private static FinnArbeidsforholdPrArbeidstakerResponse getOrCreateCurrentUserResponse() {
        FinnArbeidsforholdPrArbeidstakerResponse response = responses.get(SubjectHandler.getSubjectHandler().getUid());
        if (response == null) {
            response = new FinnArbeidsforholdPrArbeidstakerResponse();
            responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
        }
        return response;
    }

    public static void settArbeidsforhold(String arbeidsforholdData) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(Aktoer.class, new AktoerDeserializer());
            mapper.registerModule(module);
            final FinnArbeidsforholdPrArbeidstakerResponse response = mapper.readValue(arbeidsforholdData, FinnArbeidsforholdPrArbeidstakerResponse.class);
            responses.replace(SubjectHandler.getSubjectHandler().getUid(), response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void slettAlleArbeidsforhold(){
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        responses.replace(SubjectHandler.getSubjectHandler().getUid(), response);
    }
}
