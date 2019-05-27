package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.Forskuddstrekk;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.Inntekt;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.OppgaveInntektsmottaker;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.SkattbarInntekt;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.rest.RestUtils;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.getenv;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class SkattbarInntektService {
    @Value("${skatteetaten.inntektsmottaker.url}")
    private String endpoint;
    private static Logger log = LoggerFactory.getLogger(SkattbarInntektService.class);
    public Function<Sokedata, RestCallContext> restCallContextSelector;
    private DateTimeFormatter arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    public String mockFil = "/mockdata/InntektOgSkatt.json";
    ;

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

        return filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(mapTilUtbetalinger(hentOpplysninger(getRequest(sokedata))));
    }

    public List<Utbetaling> filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(List<Utbetaling> utbetalinger) {
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
                    return grupperEtterUtbetalingsStartDato(u)
                            .get(nyesteDato);
                })
                .collect(toList())
                .stream()
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public Invocation.Builder getRequest(Sokedata sokedata) {
        RestCallContext executionContext = restCallContextSelector.apply(sokedata);
        return lagRequest(executionContext, sokedata);
    }

    private Invocation.Builder lagRequest(RestCallContext executionContext, Sokedata sokedata) {
        String apiKey = getenv("SRVSOKNADSOSIALHJELP_SERVER_INNTEKTSMOTTAKER_CREDENTIALS_PASSWORD");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        WebTarget b = executionContext.getClient().target(String.format("%s%s/oppgave/inntekt", endpoint, sokedata.identifikator))
                .queryParam("fraOgMed", sokedata.fom.format(formatter))
                .queryParam("tilOgMed", sokedata.tom.format(formatter));
        return b.request().header("x-nav-apiKey", apiKey);
    }

    private List<Utbetaling> mapTilUtbetalinger(SkattbarInntekt skattbarInntekt) {
        if (skattbarInntekt == null) {
            return null;
        }
        List<Utbetaling> utbetalingerLonn = new ArrayList<>();
        List<Utbetaling> utbetalingerPensjon = new ArrayList<>();
        List<Utbetaling> dagmammaIEgenBolig = new ArrayList<>();
        List<Utbetaling> lottOgPartInnenFiske = new ArrayList<>();

        skattbarInntekt.oppgaveInntektsmottaker.forEach(oppgaveInntektsmottaker -> {
            YearMonth kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter);
            LocalDate fom = kalenderManed.atDay(1);
            LocalDate tom = kalenderManed.atEndOfMonth();
            oppgaveInntektsmottaker.inntekt.stream()
                    .filter(inntekt -> inntekt.inngaarIGrunnlagForTrekk)
                    .collect(toList()).forEach(inntekt -> {
                if (inntekt.loennsinntekt != null) {
                    utbetalingerLonn.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Lønn"));
                }
                if (inntekt.pensjonEllerTrygd != null) {
                    utbetalingerPensjon.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Pensjon"));
                }
                if (inntekt.dagmammaIEgenBolig != null) {
                    dagmammaIEgenBolig.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Dagmamma i egen bolig"));
                }
                if (inntekt.lottOgPartInnenFiske != null) {
                    lottOgPartInnenFiske.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, inntekt, "Lott og part innen fiske"));
                }
            });
        });

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
        aggregertUtbetaling.addAll(utbetalingerLonn);
        aggregertUtbetaling.addAll(utbetalingerPensjon);
        aggregertUtbetaling.addAll(dagmammaIEgenBolig);
        aggregertUtbetaling.addAll(lottOgPartInnenFiske);
        aggregertUtbetaling.addAll(forskuddstrekk);

        return summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSsamletUtbetaling(aggregertUtbetaling, forskuddstrekk);
    }

    private List<Utbetaling> summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSsamletUtbetaling(List<Utbetaling> utbetalinger, List<Utbetaling> trekk) {
        Map<String, List<Utbetaling>> collect = grupperEtterOrganisasjon(utbetalinger);
        List<Utbetaling> sum = new ArrayList<>();
        for (List<Utbetaling> value : collect.values()) {
            Map<LocalDate, List<Utbetaling>> utbetalingerPerMaaned = grupperEtterUtbetalingsStartDato(value);
            for (List<Utbetaling> utbetaling : utbetalingerPerMaaned.values()) {
                summerSammenUtbetalinger(utbetaling).ifPresent(sum::add);
            }
        }
        Map<String, List<Utbetaling>> trekkPerOrg = grupperEtterOrganisasjon(trekk);

        List<Utbetaling> retur = new ArrayList<>();
        for (Map.Entry<String, List<Utbetaling>> orgUtbetalinger : grupperEtterOrganisasjon(sum).entrySet()) {
            Map<LocalDate, List<Utbetaling>> utbetalingPerMaaned = grupperEtterUtbetalingsStartDato(orgUtbetalinger.getValue());

            List<Utbetaling> trekkForOrganisasjon = trekkPerOrg.get(orgUtbetalinger.getKey());
            for (Utbetaling utbetaling : trekkForOrganisasjon) {
                summerSammenUtbetalinger(utbetalingPerMaaned.get(utbetaling.periodeFom)).ifPresent(u -> utbetaling.brutto = u.brutto);
                retur.add(utbetaling);
            }
        }

        return retur;
    }

    private Optional<Utbetaling> summerSammenUtbetalinger(List<Utbetaling> utbetaling) {
        return utbetaling.stream().reduce((u1, u2) -> {
            u1.brutto += u2.brutto;
            u1.skattetrekk += u2.skattetrekk;
            return u1;
        });
    }

    private Map<LocalDate, List<Utbetaling>> grupperEtterUtbetalingsStartDato(List<Utbetaling> value) {
        return value.stream().collect(groupingBy(utbetaling -> utbetaling.periodeFom));
    }

    private Map<String, List<Utbetaling>> grupperEtterOrganisasjon(List<Utbetaling> sum) {
        return sum.stream().collect(groupingBy(utbetaling2 -> utbetaling2.orgnummer));
    }

    private Utbetaling getUtbetaling(OppgaveInntektsmottaker oppgaveInntektsmottaker, LocalDate fom, LocalDate tom, Inntekt inntekt, String tittel) {
        Utbetaling utbetaling = new Utbetaling();
        utbetaling.tittel = tittel;
        utbetaling.brutto = inntekt.beloep;
        utbetaling.periodeFom = fom;
        utbetaling.periodeTom = tom;
        utbetaling.type = "skatteopplysninger";
        utbetaling.orgnummer = oppgaveInntektsmottaker.virksomhetId;
        return utbetaling;
    }

    private SkattbarInntekt hentOpplysninger(Invocation.Builder request) {
        try (Response response = request.get()) {

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
                log.warn(String.format("Klarer ikke hente skatteopplysninger %s status %s ", melding, response.getStatus()));

                return new SkattbarInntekt();
            }
        } catch (RuntimeException e) {
            log.warn("Klarer ikke hente skatteopplysninger", e);
            return null;
        }
    }

    private SkattbarInntekt mockRespons() {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(mockFil);
            if (resourceAsStream == null) {
                return null;
            }
            String json = IOUtils.toString(resourceAsStream);
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, SkattbarInntekt.class);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
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
