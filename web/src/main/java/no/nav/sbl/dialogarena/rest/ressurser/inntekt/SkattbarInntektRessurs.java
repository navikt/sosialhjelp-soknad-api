package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk")
@Timed
@Produces(APPLICATION_JSON)
public class SkattbarInntektRessurs {

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private LegacyHelper legacyHelper;
    List<JsonOkonomiOpplysningUtbetaling> mockUtbetalinger;

    @GET
    public SkattbarInntektOgForskuddstrekk hentSkattbareInntekter(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getSubjectHandler().getUid();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger;
        if (mockUtbetalinger != null) {
            utbetalinger = mockUtbetalinger;
        } else {
            JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, true).getJsonInternalSoknad();
            utbetalinger = soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        }
        List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger =
                utbetalinger.stream()
                        .filter(jsonOkonomiOpplysningUtbetaling -> jsonOkonomiOpplysningUtbetaling.getType() != null &&
                                jsonOkonomiOpplysningUtbetaling.getType().equals("skatteetaten")).collect(Collectors.toList());

        Map<JsonOrganisasjon, List<JsonOkonomiOpplysningUtbetaling>> utbetalingPerOrganisasjon = skatteopplysninger
                .stream()
                .collect(Collectors.groupingBy(JsonOkonomiOpplysningUtbetaling::getOrganisasjon));

        Double samletSkattbarInntekt = skatteopplysninger.stream().map(JsonOkonomiOpplysningUtbetaling::getBrutto).reduce(Double::sum).orElse(0.0);
        Double samletTrekk = skatteopplysninger.stream().map(JsonOkonomiOpplysningUtbetaling::getSkattetrekk).reduce(Double::sum).orElse(0.0);

        return new SkattbarInntektOgForskuddstrekk()
                .withSamletInntekt(samletSkattbarInntekt)
                .withSamletTrekk(samletTrekk)
                .withLonn(getUtbetaling(utbetalingPerOrganisasjon, "Lønn"))
                .withPensjonEllerTrygd(getUtbetaling(utbetalingPerOrganisasjon, "PensjonEllerTrygd"))
                .withLottOgPartInnenfiske(getUtbetaling(utbetalingPerOrganisasjon, "LottogPartInnenFiske"));
    }

    private List<Utbetaling> getUtbetaling(Map<JsonOrganisasjon, List<JsonOkonomiOpplysningUtbetaling>> utbetalingPerOrganisasjon, String tittel) {
        List<Utbetaling> utbetaling = new ArrayList<>();
        for (Map.Entry<JsonOrganisasjon, List<JsonOkonomiOpplysningUtbetaling>> entry : utbetalingPerOrganisasjon.entrySet()) {
            JsonOrganisasjon organisasjon = entry.getKey();

            Map<String, List<JsonOkonomiOpplysningUtbetaling>> utbetalingPerTittel = entry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(JsonOkonomiOpplysningUtbetaling::getTittel));

            getUtbetaling(tittel, organisasjon, utbetalingPerTittel).ifPresent(utbetaling::add);
        }
        return utbetaling;
    }

    private Optional<Utbetaling> getUtbetaling(String tittel, JsonOrganisasjon organisasjon, Map<String, List<JsonOkonomiOpplysningUtbetaling>> utbetalingPerTittel) {
        Double trekkpliktig;
        if (utbetalingPerTittel.containsKey(tittel)) {
            trekkpliktig = utbetalingPerTittel.get(tittel).stream().map(JsonOkonomiOpplysningUtbetaling::getBrutto).reduce(Double::sum).orElse(0.0);
        } else {
            return Optional.empty();
        }

        Double skattetrekk = null;
        if (utbetalingPerTittel.containsKey("Forskuddstrekk")) {
            skattetrekk = utbetalingPerTittel.get("Forskuddstrekk").stream().map(JsonOkonomiOpplysningUtbetaling::getSkattetrekk).reduce(Double::sum).orElse(0.0);
        }


        Optional<JsonOkonomiOpplysningUtbetaling> first = utbetalingPerTittel.get(tittel).stream().findFirst();
        return Optional.of(new Utbetaling()
                .withFom(first.map(JsonOkonomiOpplysningUtbetaling::getPeriodeFom).orElse(null))
                .withTom(first.map(JsonOkonomiOpplysningUtbetaling::getPeriodeTom).orElse(null))
                .withOrganisasjon(organisasjon.getNavn())
                .withOrgnr(organisasjon.getOrganisasjonsnummer())
                .withTrekkpliktig(trekkpliktig)
                .withForskuddstrekk(skattetrekk));
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SkattbarInntektOgForskuddstrekk {
        public Double samletInntekt;
        public Double samletTrekk;
        public List<Utbetaling> lonn;
        public List<Utbetaling> pensjonEllerTrygd;
        public List<Utbetaling> lottOgPartInnenfiske;

        public SkattbarInntektOgForskuddstrekk withSamletInntekt(Double samletInntekt) {
            this.samletInntekt = samletInntekt;
            return this;
        }

        public SkattbarInntektOgForskuddstrekk withSamletTrekk(Double samletTrekk) {
            this.samletTrekk = samletTrekk;
            return this;
        }

        public SkattbarInntektOgForskuddstrekk withLonn(List<Utbetaling> lonn) {
            this.lonn = lonn;
            return this;
        }

        public SkattbarInntektOgForskuddstrekk withPensjonEllerTrygd(List<Utbetaling> pensjonEllerTrygd) {
            this.pensjonEllerTrygd = pensjonEllerTrygd;
            return this;
        }

        public SkattbarInntektOgForskuddstrekk withLottOgPartInnenfiske(List<Utbetaling> lottOgPartInnenfiske) {
            this.lottOgPartInnenfiske = lottOgPartInnenfiske;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Utbetaling {
        public String fom;
        public String tom;

        public String organisasjon;
        public String orgnr;
        public Double trekkpliktig;
        public Double forskuddstrekk;

        public Utbetaling withOrganisasjon(String organisasjon) {
            this.organisasjon = organisasjon;
            return this;
        }

        public Utbetaling withOrgnr(String orgnr) {
            this.orgnr = orgnr;
            return this;
        }

        public Utbetaling withTrekkpliktig(Double trekkpliktig) {
            this.trekkpliktig = trekkpliktig;
            return this;
        }

        public Utbetaling withForskuddstrekk(Double forskuddstrekk) {
            this.forskuddstrekk = forskuddstrekk;
            return this;
        }

        public Utbetaling withFom(String fom) {
            this.fom = fom;
            return this;
        }

        public Utbetaling withTom(String tom) {
            this.tom = tom;
            return this;
        }

    }
}