package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
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
        } catch (Exception err) {
            System.out.println(err);
        }

        return mock;
    }

    private static FinnArbeidsforholdPrArbeidstakerResponse getOrCreateCurrentUserResponse() {
        FinnArbeidsforholdPrArbeidstakerResponse response = responses.get(SubjectHandler.getUserIdFromToken());
        if (response == null) {
            response = new FinnArbeidsforholdPrArbeidstakerResponse();
            responses.put(SubjectHandler.getUserIdFromToken(), response);
        }

        return response;
    }

    public static void setArbeidsforhold(String arbeidsforholdData) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(Aktoer.class, new AktoerDeserializer());
            mapper.registerModule(module);
            final FinnArbeidsforholdPrArbeidstakerResponse response = mapper.readValue(arbeidsforholdData, FinnArbeidsforholdPrArbeidstakerResponse.class);
            if (responses.get(SubjectHandler.getUserIdFromToken()) == null){
                responses.put(SubjectHandler.getUserIdFromToken(), response);
            } else {
                responses.replace(SubjectHandler.getUserIdFromToken(), response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetArbeidsforhold(){
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        responses.replace(SubjectHandler.getUserIdFromToken(), response);
    }
}
