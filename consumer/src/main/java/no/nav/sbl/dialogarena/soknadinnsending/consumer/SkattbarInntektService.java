package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.Forskuddstrekk;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.Inntekt;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.OppgaveInntektsmottaker;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.SkattbarInntekt;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.rest.RestUtils;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.getenv;

@Service
public class SkattbarInntektService {
    @Value("${skatteetaten.inntektsmottaker.url}")
    private String endpoint;
    private static Logger log = LoggerFactory.getLogger(SkattbarInntektService.class);
    public Function<Sokedata, RestCallContext> restCallContextSelector;
    private DateTimeFormatter arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public SkattbarInntektService() {
        restCallContextSelector = (sokedata -> new RestCallContext.Builder()
                .withClient(RestUtils.createClient(RestUtils.RestConfig.builder().readTimeout(30000).build()))
                .withConcurrentRequests(2)
                .withMaximumQueueSize(6)
                .withExecutorTimeoutInMilliseconds(30000)
                .build());
    }

    public List<Utbetaling> hentSkattbarInntekt(String fnummer) {

        Sokedata sokedata = new Sokedata()
                .withFom(LocalDate.now().minusMonths(LocalDate.now().getDayOfMonth() > 10 ? 1 : 2))
                .withTom(LocalDate.now()).withIdentifikator(fnummer);

        if (Boolean.valueOf(System.getProperty("tillatmock", "false"))) {
            return mapTilUtbetalinger(mockRespons());
        }
// Skal bort før prodsetting
        List<String> fnummerTestPerson = new ArrayList<>();
        fnummerTestPerson.add("01029413157");
        fnummerTestPerson.add("04057849687");
        fnummerTestPerson.add("18017749532");
        fnummerTestPerson.add("18018200283");
        fnummerTestPerson.add("07078600378");
        fnummerTestPerson.add("02099900434");
        fnummerTestPerson.add("02116049964");

        sokedata = new Sokedata()
                .withFom(LocalDate.now().minusMonths(LocalDate.now().getDayOfMonth() > 10 ? 1 : 2))
                .withTom(LocalDate.now()).withIdentifikator(fnummerTestPerson.get(new Random().nextInt(fnummerTestPerson.size())));

        return mapTilUtbetalinger(hentOpplysninger(getRequest(sokedata)));
    }

    public Invocation.Builder getRequest(Sokedata sokedata) {
        RestCallContext executionContext = restCallContextSelector.apply(sokedata);
        return lagRequest(executionContext, sokedata);
    }

    private Invocation.Builder lagRequest(RestCallContext executionContext, Sokedata sokedata) {
        String apiKey = getenv("soknadsosialhjelp-server-eksternapp.skatt.datasamarbeid.api.inntektsmottaker-apiKey"); //https://fasit.adeo.no/resources/7504820????
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        WebTarget b = executionContext.getClient().target(String.format("%s/%s/oppgave/inntekt", endpoint, sokedata.identifikator))
                .queryParam("fraOgMed", sokedata.fom.format(formatter))
                .queryParam("tilOgMed", sokedata.tom.format(formatter));
        return b.request().header("x-nav-apiKey", apiKey);
    }

    private List<Utbetaling> mapTilUtbetalinger(SkattbarInntekt skattbarInntekt) {
        List<Utbetaling> utbetalingerLonn = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.loennsinntekt != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "Lønn";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerLonn.add(utbetaling);
                }
            }
        }

        List<Utbetaling> utbetalingerPensjon = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.pensjonEllerTrygd != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "PensjonEllerTrygd";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }
        List<Utbetaling> aldersUfoereEtterlatteAvtalefestetOgKrigspensjon = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.aldersUfoereEtterlatteAvtalefestetOgKrigspensjon != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "AldersUfoereEtterlatteAvtalefestetOgKrigspensjon";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }

        List<Utbetaling> dagmammaIEgenBolig = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.dagmammaIEgenBolig != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "DagmammaIEgenBolig";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }

        List<Utbetaling> lottOgPartInnenFiske = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.lottOgPartInnenFiske != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "LottOgPartInnenFiske";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }

        List<Utbetaling> naringsinntekt = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.naeringsinntekt != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "Naeringsinntekt";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }

        List<Utbetaling> ytelseFraOffentlige = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Inntekt inntekt : oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(Collectors.toList())) {
                if (inntekt.ytelseFraOffentlige != null) {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.tittel = "YtelseFraOffentlige";
                    utbetaling.brutto = inntekt.beloep;
                    utbetaling.periodeFom = fom;
                    utbetaling.periodeTom = tom;
                    utbetaling.type = "skatteopplysninger";
                    utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                    utbetalingerPensjon.add(utbetaling);
                }
            }
        }

        List<Utbetaling> forskuddstrekk = new ArrayList<>();
        for (OppgaveInntektsmottaker oppgaveInntektsmottaker : skattbarInntekt.oppgaveInntektsmottaker) {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            for (Forskuddstrekk f : oppgaveInntektsmottaker.forskuddstrekk) {
                Utbetaling utbetaling = new Utbetaling();
                utbetaling.tittel = "Forskuddstrekk";
                utbetaling.skattetrekk = f.beloep;
                utbetaling.periodeFom = fom;
                utbetaling.periodeTom = tom;
                utbetaling.type = "skatteopplysninger";
                utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
                forskuddstrekk.add(utbetaling);
            }
        }


        List<Utbetaling> aggregertUtbetaling = new ArrayList<>();
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(utbetalingerLonn));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(utbetalingerPensjon));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(aldersUfoereEtterlatteAvtalefestetOgKrigspensjon));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(dagmammaIEgenBolig));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(lottOgPartInnenFiske));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(naringsinntekt));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(ytelseFraOffentlige));
        aggregertUtbetaling.addAll(trekkUtUtbetalinger(forskuddstrekk));

        return aggregertUtbetaling;
    }

    private List<Utbetaling> trekkUtUtbetalinger(List<Utbetaling> utbetalinger) {
        List<Utbetaling> aggregertUtbetaling
                = new ArrayList<>();
        Map<String, List<Utbetaling>> utbetalingerPerOrganisasjon = utbetalinger.stream().collect(Collectors.groupingBy(utbetaling -> utbetaling.orgnummer));
        for (Map.Entry<String, List<Utbetaling>> entry : utbetalingerPerOrganisasjon.entrySet()) {
            entry.getValue().stream().reduce((u1, u2) -> {
                u1.brutto += u2.brutto;
                u1.skattetrekk += u2.skattetrekk;
                return u1;
            }).ifPresent(aggregertUtbetaling::add);
        }
        return aggregertUtbetaling;
    }

    private SkattbarInntekt hentOpplysninger(Invocation.Builder request) {
        Response response = null;
        try {
            response = request.get();

            if (log.isDebugEnabled()) {
                response.bufferEntity();
                log.debug("Response (" + response.getStatus() + "): " + response.readEntity(String.class));
            }

            if (response.getStatus() == 200) {

                return response.readEntity(SkattbarInntekt.class);
            } else if (response.getStatus() == 404) {
                // Ingen funnet
                return new SkattbarInntekt();
            } else {
                String melding = response.readEntity(String.class);
                throw new RuntimeException(melding);
            }
        } catch (RuntimeException e) {
            log.info("Noe uventet gikk galt ved oppslag av skattbar inntekt", e);
            throw new TjenesteUtilgjengeligException("Inntekts- og skatteopplysninger", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private SkattbarInntekt mockRespons() {
        try {
            String json = IOUtils.toString(this.getClass().getResourceAsStream("/mockdata/InntektOgSkatt.json"));
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, SkattbarInntekt.class);
        } catch (IOException e) {
            log.error("", e);
        }
        return new SkattbarInntekt();
    }


    public static class Sokedata {
        //Builder med personidentifikator og fom tom, brukes som parametere til rest kallet
        public String identifikator;
        public LocalDate fom;
        public LocalDate tom;

        public Sokedata withIdentifikator(String identifikator) {
            this.identifikator = identifikator;
            return this;
        }

        public Sokedata withFom(LocalDate fom) {
            this.fom = fom;
            return this;
        }

        public Sokedata withTom(LocalDate tom) {
            this.tom = tom;
            return this;
        }

        @Override
        public String toString() {
            return "Sokedata{" +
                    "identifikator='" + identifikator + '\'' +
                    ", fom=" + fom +
                    ", tom=" + tom +
                    '}';
        }
    }
}
