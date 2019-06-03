package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling.UtbetalingBolk;
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
import java.util.*;
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
    public List<SkattbarInntektOgForskuddstrekk> hentSkattbareInntekter(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getSubjectHandler().getUid();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger;
        if (mockUtbetalinger != null && Boolean.valueOf(System.getProperty("tillatmock"))) {
            utbetalinger = mockUtbetalinger;
        } else {
            JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, true).getJsonInternalSoknad();
            utbetalinger = soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        }

        utbetalinger = utbetalinger.stream().filter(u -> u.getTittel() != null).collect(Collectors.toList());
        List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger =
                utbetalinger.stream()
                        .filter(jsonOkonomiOpplysningUtbetaling -> jsonOkonomiOpplysningUtbetaling.getType() != null &&
                                jsonOkonomiOpplysningUtbetaling.getType().equals(UtbetalingBolk.SKATTEETATEN)).collect(Collectors.toList());

        return organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(skatteopplysninger);
    }

    private List<SkattbarInntektOgForskuddstrekk> organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger) {

        List<SkattbarInntektOgForskuddstrekk> skattbarInntektOgForskuddstrekkListe = new ArrayList<>();
        for (List<JsonOkonomiOpplysningUtbetaling> utbetalingerPerManed : new TreeMap<>(skatteopplysninger
                .stream()
                .collect(Collectors.groupingBy(JsonOkonomiOpplysningUtbetaling::getPeriodeFom))).values()) {
            Double samletSkattbarInntekt = utbetalingerPerManed.stream().map(JsonOkonomiOpplysningUtbetaling::getBrutto).reduce(Double::sum).orElse(0.0);
            Double samletTrekk = utbetalingerPerManed.stream().map(JsonOkonomiOpplysningUtbetaling::getSkattetrekk).reduce(Double::sum).orElse(0.0);

            Map<JsonOrganisasjon, List<JsonOkonomiOpplysningUtbetaling>> utbetalingPerOrganisasjon = utbetalingerPerManed
                    .stream()
                    .collect(Collectors.groupingBy(JsonOkonomiOpplysningUtbetaling::getOrganisasjon));

            List<Organisasjon> organisasjoner = new ArrayList<>();
            for (Map.Entry<JsonOrganisasjon, List<JsonOkonomiOpplysningUtbetaling>> jsonOrganisasjonListEntry : utbetalingPerOrganisasjon.entrySet()) {
                List<Utbetaling> utbetalingListe = new ArrayList<>();

                JsonOrganisasjon jsonOrganisasjon = jsonOrganisasjonListEntry.getKey();
                for (JsonOkonomiOpplysningUtbetaling jsonOkonomiOpplysningUtbetaling : jsonOrganisasjonListEntry.getValue()) {
                    Utbetaling utbetaling = new Utbetaling()
                            .withTittel(jsonOkonomiOpplysningUtbetaling.getTittel())
                            .withBrutto(jsonOkonomiOpplysningUtbetaling.getBrutto())
                            .withForskuddstrekk(jsonOkonomiOpplysningUtbetaling.getSkattetrekk());
                    utbetalingListe.add(utbetaling);
                }
                Optional<JsonOkonomiOpplysningUtbetaling> first = utbetalingerPerManed.stream().findFirst();

                organisasjoner.add(mapTilOrganisasjon(utbetalingListe, jsonOrganisasjon, first));
            }
            SkattbarInntektOgForskuddstrekk skattbarInntektOgForskuddstrekk = new SkattbarInntektOgForskuddstrekk()
                   .withOrganisasjoner(organisasjoner);
            skattbarInntektOgForskuddstrekkListe.add(skattbarInntektOgForskuddstrekk);
        }
        Collections.reverse(skattbarInntektOgForskuddstrekkListe);
        return skattbarInntektOgForskuddstrekkListe;
    }

    private Organisasjon mapTilOrganisasjon(List<Utbetaling> utbetalingListe, JsonOrganisasjon jsonOrganisasjon, Optional<JsonOkonomiOpplysningUtbetaling> first) {
        return new Organisasjon().withUtbetalinger(utbetalingListe)
                .withOrganisasjonsnavn(jsonOrganisasjon
                        .getNavn())
                .withOrgnr(jsonOrganisasjon
                        .getOrganisasjonsnummer())
                .withFom(first.map(JsonOkonomiOpplysningUtbetaling::getPeriodeFom).orElse(null))
                .withTom(first.map(JsonOkonomiOpplysningUtbetaling::getPeriodeTom).orElse(null));
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SkattbarInntektOgForskuddstrekk {
        public List<Organisasjon> organisasjoner;

        public SkattbarInntektOgForskuddstrekk withOrganisasjoner(List<Organisasjon> organisasjoner) {
            this.organisasjoner = organisasjoner;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Organisasjon {
        public List<Utbetaling> utbetalinger;
        public String organisasjonsnavn;
        public String orgnr;
        public String fom;
        public String tom;

        public Organisasjon withUtbetalinger(List<Utbetaling> utbetalinger) {
            this.utbetalinger = utbetalinger;
            return this;
        }

        public Organisasjon withOrganisasjonsnavn(String organisasjonsnavn) {
            this.organisasjonsnavn = organisasjonsnavn;
            return this;
        }

        public Organisasjon withOrgnr(String orgnr) {
            this.orgnr = orgnr;
            return this;
        }

        public Organisasjon withFom(String fom) {
            this.fom = fom;
            return this;
        }

        public Organisasjon withTom(String tom) {
            this.tom = tom;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Utbetaling {


        public Double brutto;
        public Double forskuddstrekk;
        public String tittel;

        public Utbetaling withBrutto(Double brutto) {
            this.brutto = brutto;
            return this;
        }

        public Utbetaling withForskuddstrekk(Double forskuddstrekk) {
            this.forskuddstrekk = forskuddstrekk;
            return this;
        }

        public Utbetaling withTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

    }
}