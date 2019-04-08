//package no.nav.sbl.dialogarena.sendsoknad.mockmodul.skattbarinntekt;
//
//import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
//import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
//import org.mockito.stubbing.Answer;
//
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//public class InntektOgSkatteopplysningerMock {
//    public InntektOgskatteopplysningerConsumer inntektOgSkatteopplysningerRestService() {
//        InntektOgskatteopplysningerConsumer mock = mock(InntektOgskatteopplysningerConsumer.class);
//        when(mock.sok(any(InntektOgskatteopplysningerConsumer.Sokedata.class))).thenAnswer(responsmock());
//
//        return mock;
//    }
//
//    private Answer<?> responsmock() {
//        return invocation -> {
//            InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons respons = new InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons();
//
//            respons.oppgaveInntektsmottaker = Arrays.asList(getLonnsoppgave(), getPensjonEllerTrygd(), getLottOgPartInnenFiske());
//
//            return Optional.of(mapTilUtbetalinger(respons));
//        };
//    }
//
//    private InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker getLonnsoppgave() {
//        InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker = new InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker();
//        oppgaveInntektsmottaker.kalendermaaned = "2019-03";
//        oppgaveInntektsmottaker.virksomhetsid = "911270404";
//
//        InntektOgskatteopplysningerConsumer.Inntekt inntekt = new InntektOgskatteopplysningerConsumer.Inntekt();
//        inntekt.beloep = 15000;
//        inntekt.loennsinntekt = new InntektOgskatteopplysningerConsumer.Loennsinntekt();
//
//        InntektOgskatteopplysningerConsumer.Forskuddstrekk forskuddstrekk = new InntektOgskatteopplysningerConsumer.Forskuddstrekk();
//        forskuddstrekk.beloep = 12;
//        forskuddstrekk.beskrivelse = "Forskuddstrekk";
//
//        oppgaveInntektsmottaker.inntekt = Collections.singletonList(inntekt);
//        oppgaveInntektsmottaker.forskuddstrekk = Collections.singletonList(forskuddstrekk);
//
//        inntekt.loennsinntekt = new InntektOgskatteopplysningerConsumer.Loennsinntekt();
//        return oppgaveInntektsmottaker;
//    }
//
//    private InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker getPensjonEllerTrygd() {
//        InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker = new InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker();
//        oppgaveInntektsmottaker.kalendermaaned = "2019-03";
//        oppgaveInntektsmottaker.virksomhetsid = "911270409";
//
//        InntektOgskatteopplysningerConsumer.Inntekt inntekt = new InntektOgskatteopplysningerConsumer.Inntekt();
//        inntekt.beloep = 1000;
//
//        InntektOgskatteopplysningerConsumer.Forskuddstrekk forskuddstrekk = new InntektOgskatteopplysningerConsumer.Forskuddstrekk();
//        forskuddstrekk.beloep = 20;
//        forskuddstrekk.beskrivelse = "Forskuddstrekk";
//
//        oppgaveInntektsmottaker.inntekt = Collections.singletonList(inntekt);
//        oppgaveInntektsmottaker.forskuddstrekk = Collections.singletonList(forskuddstrekk);
//
//        inntekt.pensjonEllerTrygd = new InntektOgskatteopplysningerConsumer.PensjonEllerTrygd();
//        return oppgaveInntektsmottaker;
//    }
//
//    private InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker getLottOgPartInnenFiske() {
//        InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker = new InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker();
//        oppgaveInntektsmottaker.kalendermaaned = "2019-03";
//        oppgaveInntektsmottaker.virksomhetsid = "911270400";
//
//        InntektOgskatteopplysningerConsumer.Inntekt inntekt = new InntektOgskatteopplysningerConsumer.Inntekt();
//        inntekt.beloep = 100000;
//
//        InntektOgskatteopplysningerConsumer.Forskuddstrekk forskuddstrekk = new InntektOgskatteopplysningerConsumer.Forskuddstrekk();
//        forskuddstrekk.beloep = 2000;
//        forskuddstrekk.beskrivelse = "Forskuddstrekk";
//
//        oppgaveInntektsmottaker.inntekt = Collections.singletonList(inntekt);
//        oppgaveInntektsmottaker.forskuddstrekk = Collections.singletonList(forskuddstrekk);
//
//        inntekt.lottOgPartInnenFiske = new InntektOgskatteopplysningerConsumer.LottOgPartInnenFiske();
//        return oppgaveInntektsmottaker;
//    }
//
//    private List<Utbetaling> mapTilUtbetalinger(InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons respons) {
//
//        DateTimeFormatter arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
//
//
//        List<Utbetaling> utbetalingerLonn = new ArrayList<>();
//        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
//            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
//            LocalDate fom = kalenderManed.atDay(1);
//            LocalDate tom = kalenderManed.atEndOfMonth();
//            for (InntektOgskatteopplysningerConsumer.Inntekt inntekt : oppgaveInntektsmottaker.inntekt) {
//                if (inntekt.loennsinntekt != null) {
//                    Utbetaling utbetaling = new Utbetaling();
//                    utbetaling.tittel = "Lønn";
//                    utbetaling.brutto = inntekt.beloep;
//                    utbetaling.periodeFom = fom;
//                    utbetaling.periodeTom = tom;
//                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetsid;
//                    utbetaling.type = "skatteetaten";
//                    utbetalingerLonn.add(utbetaling);
//                }
//            }
//        }
//
//        List<Utbetaling> utbetalingerPensjon = new ArrayList<>();
//        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
//            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
//            LocalDate fom = kalenderManed.atDay(1);
//            LocalDate tom = kalenderManed.atEndOfMonth();
//            for (InntektOgskatteopplysningerConsumer.Inntekt inntekt : oppgaveInntektsmottaker.inntekt) {
//                if (inntekt.pensjonEllerTrygd != null) {
//                    Utbetaling utbetaling = new Utbetaling();
//                    utbetaling.tittel = "PensjonEllerTrygd";
//                    utbetaling.brutto = inntekt.beloep;
//                    utbetaling.periodeFom = fom;
//                    utbetaling.periodeTom = tom;
//                    utbetaling.type = "skatteetaten";
//                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetsid;
//                    utbetalingerPensjon.add(utbetaling);
//                }
//            }
//        }
//
//        List<Utbetaling> utbetalingLottogPartInnenFiske = new ArrayList<>();
//        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
//            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
//            LocalDate fom = kalenderManed.atDay(1);
//            LocalDate tom = kalenderManed.atEndOfMonth();
//            for (InntektOgskatteopplysningerConsumer.Inntekt inntekt : oppgaveInntektsmottaker.inntekt) {
//                if (inntekt.lottOgPartInnenFiske != null) {
//                    Utbetaling utbetaling = new Utbetaling();
//                    utbetaling.tittel = "LottogPartInnenFiske";
//                    utbetaling.brutto = inntekt.beloep;
//                    utbetaling.periodeFom = fom;
//                    utbetaling.periodeTom = tom;
//                    utbetaling.type = "skatteetaten";
//                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetsid;
//                    utbetalingLottogPartInnenFiske.add(utbetaling);
//                }
//            }
//        }
//
//        List<Utbetaling> forskuddstrekk = new ArrayList<>();
//        for (InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker : respons.oppgaveInntektsmottaker) {
//            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
//            LocalDate fom = kalenderManed.atDay(1);
//            LocalDate tom = kalenderManed.atEndOfMonth();
//            for (InntektOgskatteopplysningerConsumer.Forskuddstrekk f : oppgaveInntektsmottaker.forskuddstrekk) {
//                Utbetaling utbetaling = new Utbetaling();
//                utbetaling.tittel = f.beskrivelse;
//                utbetaling.skattetrekk = f.beloep;
//                utbetaling.periodeFom = fom;
//                utbetaling.periodeTom = tom;
//                utbetaling.type = "skatteetaten";
//                utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetsid;
//                forskuddstrekk.add(utbetaling);
//            }
//        }
//
//
//        List<Utbetaling> utbetalinger = new ArrayList<>();
//        utbetalinger.addAll(forskuddstrekk);
//        utbetalinger.addAll(utbetalingerLonn);
//        utbetalinger.addAll(utbetalingerPensjon);
//        utbetalinger.addAll(utbetalingLottogPartInnenFiske);
//
//        return utbetalinger;
//    }
//}