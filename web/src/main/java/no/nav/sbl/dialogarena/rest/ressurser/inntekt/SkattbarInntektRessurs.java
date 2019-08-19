package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

import static java.util.stream.Collectors.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk")
@Timed
@Produces(APPLICATION_JSON)
public class SkattbarInntektRessurs {

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;


    List<JsonOkonomiOpplysningUtbetaling> mockUtbetalinger;

    @GET
    public List<SkattbarInntektOgForskuddstrekk> hentSkattbareInntekter(@PathParam("behandlingsId") String behandlingsId) {
        String eier =  OidcFeatureToggleUtils.getUserId();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger;
        if (mockUtbetalinger != null && Boolean.valueOf(System.getProperty("tillatmock"))) {
            utbetalinger = mockUtbetalinger;
        } else {
            JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
            utbetalinger = soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        }

        utbetalinger = utbetalinger.stream().filter(u -> u.getTittel() != null).collect(toList());
        List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger =
                utbetalinger.stream()
                        .filter(jsonOkonomiOpplysningUtbetaling -> jsonOkonomiOpplysningUtbetaling.getType() != null &&
                                jsonOkonomiOpplysningUtbetaling.getType().equals("skatteetaten")).collect(toList());

        return organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(skatteopplysninger);
    }

    private List<SkattbarInntektOgForskuddstrekk> organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger) {

        List<SkattbarInntektOgForskuddstrekk> skattbarInntektOgForskuddstrekkListe = new ArrayList<>();
        for (List<JsonOkonomiOpplysningUtbetaling> utbetalingerPerManed : new TreeMap<>(skatteopplysninger
                .stream()
                .collect(groupingBy(JsonOkonomiOpplysningUtbetaling::getPeriodeFom))).values()) {

            Map<Optional<JsonOrganisasjon>, List<JsonOkonomiOpplysningUtbetaling>> utbetalingPerOrganisasjon = utbetalingerPerManed
                    .stream()
                    .collect(groupingBy(utbetaling -> Optional.ofNullable(utbetaling.getOrganisasjon())));

            List<Organisasjon> organisasjoner = new ArrayList<>();
            for (Map.Entry<Optional<JsonOrganisasjon>, List<JsonOkonomiOpplysningUtbetaling>> jsonOrganisasjonListEntry : utbetalingPerOrganisasjon.entrySet()) {
                List<Utbetaling> utbetalingListe = new ArrayList<>();

                for (JsonOkonomiOpplysningUtbetaling jsonOkonomiOpplysningUtbetaling : jsonOrganisasjonListEntry.getValue()) {
                    Utbetaling utbetaling = new Utbetaling()
                            .withTittel(jsonOkonomiOpplysningUtbetaling.getTittel())
                            .withBrutto(jsonOkonomiOpplysningUtbetaling.getBrutto())
                            .withForskuddstrekk(jsonOkonomiOpplysningUtbetaling.getSkattetrekk());
                    utbetalingListe.add(utbetaling);
                }
                Optional<JsonOkonomiOpplysningUtbetaling> first = utbetalingerPerManed.stream().findFirst();

                JsonOrganisasjon jsonOrganisasjon;
                if (jsonOrganisasjonListEntry.getKey().isPresent()) {
                    jsonOrganisasjon = jsonOrganisasjonListEntry.getKey().get();
                } else {
                    jsonOrganisasjon = new JsonOrganisasjon().withNavn("Uten organisasjonsnummer");
                }
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