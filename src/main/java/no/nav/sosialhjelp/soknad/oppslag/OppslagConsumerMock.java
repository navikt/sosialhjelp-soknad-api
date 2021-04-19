package no.nav.sosialhjelp.soknad.oppslag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;
import no.nav.sosialhjelp.soknad.oppslag.dto.UtbetalingDto;
import no.nav.sosialhjelp.soknad.oppslag.dto.UtbetalingDto.KomponentDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OppslagConsumerMock {

    private static final String DEFAULT_KONTONUMMER = "11111111111";
    private static final Map<String, KontonummerDto> kontonummerResponses = new HashMap<>();
    private static final Map<String, List<UtbetalingDto>> utbetalingerResponses = new HashMap<>();

    private static KontonummerDto getOrDefaultKontonummerResponse(String fnr) {
        return kontonummerResponses.computeIfAbsent(fnr, k -> defaultKontonummer());
    }

    private static List<UtbetalingDto> getOrDefaultUtbetalingerResponse(String fnr) {
        return utbetalingerResponses.computeIfAbsent(fnr, k -> defaultUtbetalinger());
    }

    private static KontonummerDto defaultKontonummer() {
        return new KontonummerDto(DEFAULT_KONTONUMMER);
    }

    private static List<UtbetalingDto> defaultUtbetalinger() {
        return singletonList(new UtbetalingDto(
                "navytelse",
                1000.0,
                1234.0,
                200.0,
                34.0,
                "bilagsnummer",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(2),
                singletonList(new KomponentDto("type", 42.0, "sats", 21.0, 2.0)),
                "tittel",
                "orgnr"));
    }

    public OppslagConsumer oppslagMock() {
        var mock = mock(OppslagConsumer.class);

        when(mock.getKontonummer(anyString()))
                .thenAnswer(invocationOnMock -> getOrDefaultKontonummerResponse(SubjectHandler.getUserId()));
        when(mock.getUtbetalingerSiste40Dager(anyString()))
                .thenAnswer(invocationOnMock -> getOrDefaultUtbetalingerResponse(SubjectHandler.getUserId()));

        return mock;
    }

    public static void setKontonummer(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode node = mapper.readTree(json);
            String kontonummer = node.at("/person/bankkonto/bankkonto/bankkontonummer").textValue();

            kontonummerResponses.put(SubjectHandler.getUserId(), new KontonummerDto(kontonummer));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void setUtbetalinger() {
        // put default utbetaling
        utbetalingerResponses.put(SubjectHandler.getUserId(), defaultUtbetalinger());
    }
}
