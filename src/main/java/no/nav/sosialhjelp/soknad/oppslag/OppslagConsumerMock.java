package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OppslagConsumerMock {

    private static final String DEFAULT_KONTONUMMER = "11111111111";
    private static final Map<String, KontonummerDto> kontonummerResponses = new HashMap<>();

    private static KontonummerDto getOrDefaultResponse(String fnr) {
        return kontonummerResponses.computeIfAbsent(fnr, k -> defaultKontonummer());
    }

    private static KontonummerDto defaultKontonummer() {
        return new KontonummerDto(DEFAULT_KONTONUMMER);
    }

    public OppslagConsumer oppslagMock() {
        var mock = mock(OppslagConsumer.class);

        when(mock.getKontonummer(anyString()))
                .thenAnswer(invocationOnMock -> getOrDefaultResponse(SubjectHandler.getUserId()));

        return mock;
    }
}
