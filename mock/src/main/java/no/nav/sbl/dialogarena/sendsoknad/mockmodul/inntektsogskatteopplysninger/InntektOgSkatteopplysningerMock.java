package no.nav.sbl.dialogarena.sendsoknad.mockmodul.inntektsogskatteopplysninger;

import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
            oppgaveInntektsmottaker.kalendermaaned = "2019-03";

            InntektOgskatteopplysningerConsumer.Inntekt inntekt = new InntektOgskatteopplysningerConsumer.Inntekt();
            inntekt.beloep = 15000;

            InntektOgskatteopplysningerConsumer.Forskuddstrekk forskuddstrekk = new InntektOgskatteopplysningerConsumer.Forskuddstrekk();
            forskuddstrekk.beloep = 12;
            forskuddstrekk.beskrivelse = "Vanlig";

            oppgaveInntektsmottaker.inntekt = Collections.singletonList(inntekt);
            oppgaveInntektsmottaker.forskuddstrekk = Collections.singletonList(forskuddstrekk);

            inntekt.loennsinntekt = new InntektOgskatteopplysningerConsumer.Loennsinntekt();
            respons.oppgaveInntektsmottaker = Collections.singletonList(oppgaveInntektsmottaker);

            return Optional.of(mapTilUtbetalinger(respons));
        };
    }

    private List<Utbetaling> mapTilUtbetalinger(InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons respons) {

        DateTimeFormatter arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM");


        List<Utbetaling> utbetalingerLonn = new ArrayList<>();
        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (InntektOgskatteopplysningerConsumer.Inntekt inntekt : oppgaveInntektsmottaker.inntekt) {
                if (inntekt.loennsinntekt != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "Lønn";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetalingerLonn.add(utbetaling);
                }
            }
        }

        List<Utbetaling> utbetalingerPensjon = new ArrayList<>();
        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (InntektOgskatteopplysningerConsumer.Inntekt inntekt : oppgaveInntektsmottaker.inntekt) {
                if (inntekt.pensjonEllerTrygd != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "PensjonEllerTrygd";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }

        List<Utbetaling> forskuddstrekk = new ArrayList<>();
        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (InntektOgskatteopplysningerConsumer.Forskuddstrekk f : oppgaveInntektsmottaker.forskuddstrekk) {
                Utbetaling utbetaling = new Utbetaling();
                utbetaling.tittel = f.beskrivelse;
                utbetaling.skattetrekk = f.beloep;
                utbetaling.periodeFom = fom;
                utbetaling.periodeTom = tom;
                utbetaling.type = "skatteopplysninger";
                forskuddstrekk.add(utbetaling);

            }
        }


        List<Utbetaling> aggregertUtbetaling = new ArrayList<>();
        utbetalingerLonn.stream().reduce((u1, u2) -> {
            u1.brutto += u2.brutto;
            return u1;
        }).ifPresent(aggregertUtbetaling::add);

        utbetalingerPensjon.stream().reduce((u1, u2) -> {
            u1.brutto += u2.brutto;
            return u1;
        }).ifPresent(aggregertUtbetaling::add);

        forskuddstrekk.stream().reduce((u1, u2) -> {
            u1.skattetrekk += u2.skattetrekk;
            return u1;
        }).ifPresent(aggregertUtbetaling::add);


        return aggregertUtbetaling;
    }
}
