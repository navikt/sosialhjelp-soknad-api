package no.nav.sbl.dialogarena.sendsoknad.mockmodul.inntektsogskatteopplysninger;

import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InntektOgSkatteopplysningerMock {
    public InntektOgskatteopplysningerConsumer inntektOgSkatteopplysningerRestService() {
        InntektOgskatteopplysningerConsumer mock = mock(InntektOgskatteopplysningerConsumer.class);
        when(mock.sok(any(InntektOgskatteopplysningerConsumer.Sokedata.class))).thenAnswer(responsmock());

        return mock;
    }

    private Answer<?> responsmock() {
        return invocation -> {
            InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons respons = new InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons();

            InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker = new InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker();

            InntektOgskatteopplysningerConsumer.Inntekt inntekt = new InntektOgskatteopplysningerConsumer.Inntekt();
            inntekt.beloep = 15000;
            oppgaveInntektsmottaker.inntekt = Collections.singletonList(inntekt);
            inntekt.loennsinntekt = new InntektOgskatteopplysningerConsumer.Loennsinntekt();
            respons.oppgaveInntektsmottaker = Collections.singletonList(oppgaveInntektsmottaker);

            return Optional.of(mapTilUtbetalinger(respons));
        };
    }

    private List<Utbetaling> mapTilUtbetalinger(InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons repons) {
        return repons.oppgaveInntektsmottaker
                .stream()
                .flatMap(oppgaveInntektsmottaker -> oppgaveInntektsmottaker
                        .inntekt
                        .stream())
                .filter(inntekt -> inntekt
                        .loennsinntekt != null)
                .map(inntekt -> {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.type = "Lønn";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.netto = inntekt.beloep;
                    return utbetaling;
                }).collect(toList());
    }
}
