package no.nav.sosialhjelp.soknad.consumer.skatt;

import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import no.nav.sosialhjelp.soknad.skattbarinntekt.SkatteetatenClient;
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.Inntekt;
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.OppgaveInntektsmottaker;
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.SkattbarInntekt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class SkattbarInntektService {

    private final DateTimeFormatter arManedFormatter;
    private final SkatteetatenClient skatteetatenClient;

    public SkattbarInntektService(
            SkatteetatenClient skatteetatenClient
    ) {
        this.skatteetatenClient = skatteetatenClient;
        this.arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    }

    public List<Utbetaling> hentUtbetalinger(String fnummer) {
        var skattbarInntekt = skatteetatenClient.hentSkattbarinntekt(fnummer);
        return filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(mapTilUtbetalinger(skattbarInntekt));
    }

    private List<Utbetaling> filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(List<Utbetaling> utbetalinger) {
        if (utbetalinger == null) {
            return null;
        }
        return grupperEtterOrganisasjon(utbetalinger)
                .values()
                .stream()
                .map(u -> {
                    LocalDate nyesteDato = u.stream()
                            .max(Comparator.comparing(o -> o.periodeFom))
                            .orElseThrow(IllegalStateException::new)
                            .periodeFom;
                    return grupperOgsummerEtterUtbetalingsStartDato(u)
                            .get(nyesteDato);
                }).collect(Collectors.toList());
    }

    private List<Utbetaling> mapTilUtbetalinger(SkattbarInntekt skattbarInntekt) {
        if (skattbarInntekt == null) {
            return null;
        }
        List<Utbetaling> utbetalingerLonn = new ArrayList<>();
        List<Utbetaling> utbetalingerPensjon = new ArrayList<>();
        List<Utbetaling> dagmammaIEgenBolig = new ArrayList<>();
        List<Utbetaling> lottOgPartInnenFiske = new ArrayList<>();

        skattbarInntekt.getOppgaveInntektsmottaker().forEach(oppgaveInntektsmottaker -> {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.getKalendermaaned(), arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            oppgaveInntektsmottaker.getInntekt().stream()
                    .filter(Inntekt::getInngaarIGrunnlagForTrekk)
                    .collect(toList()).forEach(inntekt -> {
                        if (inntekt.getLoennsinntekt() != null) {
                            utbetalingerLonn.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Lønn"));
                        }
                        if (inntekt.getPensjonEllerTrygd() != null) {
                            utbetalingerPensjon.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Pensjon"));
                        }
                        if (inntekt.getDagmammaIEgenBolig() != null) {
                            dagmammaIEgenBolig.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Dagmamma i egen bolig"));
                        }
                        if (inntekt.getLottOgPartInnenFiske() != null) {
                            lottOgPartInnenFiske.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Lott og part innen fiske"));
                        }
                    });
        });

        List<Utbetaling> forskuddstrekk = new ArrayList<>();
        skattbarInntekt.getOppgaveInntektsmottaker().forEach(oppgaveInntektsmottaker -> {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.getKalendermaaned(), arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            oppgaveInntektsmottaker.getForskuddstrekk().forEach(f -> {
                Utbetaling utbetaling = new Utbetaling();
                utbetaling.tittel = "Forskuddstrekk";
                utbetaling.skattetrekk = f.getBeloep();
                utbetaling.periodeFom = fom;
                utbetaling.periodeTom = tom;
                utbetaling.type = "skatteopplysninger";
                utbetaling.orgnummer = oppgaveInntektsmottaker.getOpplysningspliktigId();
                forskuddstrekk.add(utbetaling);
            });
        });

        List<Utbetaling> aggregertUtbetaling = new ArrayList<>();
        aggregertUtbetaling.addAll(utbetalingerLonn);
        aggregertUtbetaling.addAll(utbetalingerPensjon);
        aggregertUtbetaling.addAll(dagmammaIEgenBolig);
        aggregertUtbetaling.addAll(lottOgPartInnenFiske);
        aggregertUtbetaling.addAll(forskuddstrekk);

        return summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSsamletUtbetaling(aggregertUtbetaling, forskuddstrekk);
    }

    private List<Utbetaling> summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSsamletUtbetaling(List<Utbetaling> utbetalinger, List<Utbetaling> trekk) {
        Map<String, Map<LocalDate, Utbetaling>> bruttoOrgPerMaaned = getUtBetalingPerMaanedPerOrg(grupperEtterOrganisasjon(utbetalinger));
        Map<String, Map<LocalDate, Utbetaling>> trekkOrgPerMaaned = getUtBetalingPerMaanedPerOrg(grupperEtterOrganisasjon(trekk));

        List<Utbetaling> utbetalingerBrutto = bruttoOrgPerMaaned.values().stream().flatMap(m -> m.values().stream()).collect(toList());
        return utbetalingerBrutto.stream().filter(utbetaling -> !utbetaling.orgnummer.equals("995277670")).peek(utbetaling -> {
            Map<LocalDate, Utbetaling> localDateUtbetalingMap = trekkOrgPerMaaned.get(utbetaling.orgnummer);
            if (localDateUtbetalingMap != null) {
                Utbetaling trekkUtbetaling = localDateUtbetalingMap.get(utbetaling.periodeFom);
                if (trekkUtbetaling != null) {
                    utbetaling.skattetrekk = trekkUtbetaling.skattetrekk;
                }
            }
            utbetaling.tittel = "Lønnsinntekt";
        }).collect(toList());
    }

    private Map<String, Map<LocalDate, Utbetaling>> getUtBetalingPerMaanedPerOrg(Map<String, List<Utbetaling>> orgUtbetaling) {
        Map<String, Map<LocalDate, Utbetaling>> bruttoOrgPerMaaned = new HashMap<>();
        for (Map.Entry<String, List<Utbetaling>> value : orgUtbetaling.entrySet()) {
            bruttoOrgPerMaaned.put(value.getKey(), grupperOgsummerEtterUtbetalingsStartDato(value.getValue()));
        }
        return bruttoOrgPerMaaned;
    }


    private Map<LocalDate, Utbetaling> grupperOgsummerEtterUtbetalingsStartDato(List<Utbetaling> utbetalinger) {
        Map<LocalDate, Utbetaling> ret = new HashMap<>();
        utbetalinger.stream().collect(groupingBy(utbetaling1 -> utbetaling1.periodeFom)).forEach((key, value) -> value.stream().reduce((utbetaling, utbetaling2) -> {
            utbetaling.brutto += utbetaling2.brutto;
            utbetaling.skattetrekk += utbetaling2.skattetrekk;
            return utbetaling;
        }).ifPresent(utbetaling -> ret.put(key, utbetaling)));
        return ret;
    }

    private Map<String, List<Utbetaling>> grupperEtterOrganisasjon(List<Utbetaling> sum) {
        return sum.stream().collect(groupingBy(utbetaling2 -> utbetaling2.orgnummer));
    }

    private Utbetaling getUtbetaling(OppgaveInntektsmottaker oppgaveInntektsmottaker, LocalDate fom, LocalDate tom, Inntekt inntekt, String tittel) {
        Utbetaling utbetaling = new Utbetaling();
        utbetaling.tittel = tittel;
        utbetaling.brutto = inntekt.getBeloep();
        utbetaling.periodeFom = fom;
        utbetaling.periodeTom = tom;
        utbetaling.type = "skatteopplysninger";
        utbetaling.orgnummer = oppgaveInntektsmottaker.getOpplysningspliktigId();
        return utbetaling;
    }

}
