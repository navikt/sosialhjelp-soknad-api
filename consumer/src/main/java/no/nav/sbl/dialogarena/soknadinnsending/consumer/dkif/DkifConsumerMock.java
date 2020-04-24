package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto.DigitalKontaktinfo;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto.DigitalKontaktinfoBolk;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DkifConsumerMock {

    private static final String telefonnummer = "12345789";
    private static Map<String, DigitalKontaktinfoBolk> responses = new HashMap<>();

    public DkifConsumer dkifConsumerMock() {
        DkifConsumer mock = mock(DkifConsumer.class);

        when(mock.hentDigitalKontaktinfo(anyString()))
                .thenAnswer((invocationOnMock) -> getOrDefaultResponse(OidcFeatureToggleUtils.getUserId()));

        return mock;
    }

    public static DigitalKontaktinfoBolk getOrDefaultResponse(String fnr) {
        DigitalKontaktinfoBolk response = responses.get(fnr);
        if (response == null) {
            response = defaultDigitalKontaktinfo(fnr);
            responses.put(fnr, response);
        }

        return response;
    }

    private static DigitalKontaktinfoBolk defaultDigitalKontaktinfo(String fnr) {
        return new DigitalKontaktinfoBolk(singletonMap(fnr,new DigitalKontaktinfo(telefonnummer)), null);
    }

    public static void setTelefonnummer(String telefonnummer, String fnr) {
        DigitalKontaktinfoBolk response = new DigitalKontaktinfoBolk(singletonMap(fnr, new DigitalKontaktinfo(telefonnummer)), null);
        responses.put(fnr, response);
    }

    public static void resetTelefonnummer(String fnr) {
        responses.replace(fnr, defaultDigitalKontaktinfo(fnr));
    }
}
