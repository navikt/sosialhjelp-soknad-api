package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.removeBekreftelserIfPresent;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk")
@Timed
@Produces(APPLICATION_JSON)
public class SkattbarInntektRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SkattetatenSystemdata skattetatenSystemdata;

    @Inject
    private TextService textService;

    @GET
    public SkattbarInntektFrontend hentSkattbareInntekter(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger;

        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        utbetalinger = soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();

        List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger =
                utbetalinger.stream()
                        .filter(u -> u.getTittel() != null)
                        .filter(jsonOkonomiOpplysningUtbetaling -> jsonOkonomiOpplysningUtbetaling.getType() != null &&
                                jsonOkonomiOpplysningUtbetaling.getType().equals(UTBETALING_SKATTEETATEN)).collect(toList());

        return new SkattbarInntektFrontend()
                .withInntektFraSkatteetaten(organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(skatteopplysninger))
                .withInntektFraSkatteetatenFeilet(soknad.getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet())
                .withInntektFraSkatteetatenSamtykke(hentSamtykkeBooleanFraSoknad(soknad), hentSamtykkeDatoFraSoknad(soknad));
    }

    @POST
    @Path(value = "/samtykke")
    public void updateSamtykke(@PathParam("behandlingsId") String behandlingsId, boolean samtykke,
                               @HeaderParam(value = AUTHORIZATION) String token) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();

        boolean lagretSamtykke = hentSamtykkeBooleanFraSoknad(soknad.getJsonInternalSoknad());

        if(lagretSamtykke != samtykke) {
            removeBekreftelserIfPresent(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE);
            setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, samtykke, textService.getJsonOkonomiTittel("utbetalinger.skattbar.samtykke"));
        }

        skattetatenSystemdata.updateSystemdataIn(soknad, token);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private boolean hentSamtykkeBooleanFraSoknad(JsonInternalSoknad soknad) {
        return soknad.getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(UTBETALING_SKATTEETATEN_SAMTYKKE))
                .anyMatch(JsonOkonomibekreftelse::getVerdi);
    }

    private String hentSamtykkeDatoFraSoknad(JsonInternalSoknad soknad) {
        return soknad.getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(UTBETALING_SKATTEETATEN_SAMTYKKE))
                .filter(JsonOkonomibekreftelse::getVerdi)
                .findAny()
                .map(JsonOkonomibekreftelse::getBekreftelsesDato).orElse(null);
    }

    private List<SkattbarInntektOgForskuddstrekk> organiserSkattOgForskuddstrekkEtterMaanedOgOrganisasjon(List<JsonOkonomiOpplysningUtbetaling> skatteopplysninger) {
        List<SkattbarInntektOgForskuddstrekk> skattbarInntektOgForskuddstrekkListe = new ArrayList<>();

        // Skatteetaten returnerer opplysninger månedsvis, så objekter med samme PeriodeFom gjelder for samme periode
        Map<Optional<String>, Map<Optional<JsonOrganisasjon>, List<JsonOkonomiOpplysningUtbetaling>>> utbetalingerPerManedPerOrganisasjon = skatteopplysninger.stream().collect(
                groupingBy(utbetaling -> Optional.ofNullable(utbetaling.getPeriodeFom()),
                        groupingBy(utbetaling -> Optional.ofNullable(utbetaling.getOrganisasjon()))));

        for (Map<Optional<JsonOrganisasjon>, List<JsonOkonomiOpplysningUtbetaling>> utbetalingerInnenforSammeManed : utbetalingerPerManedPerOrganisasjon.values()) {
            List<Organisasjon> organisasjoner = new ArrayList<>();
            utbetalingerInnenforSammeManed.forEach((organisasjon, utbetalinger) -> {
                List<Utbetaling> utbetalingListe = utbetalinger.stream().map(this::mapTilUtbetaling).collect(toList());

                JsonOrganisasjon jsonOrganisasjon = organisasjon.orElse(new JsonOrganisasjon().withNavn("Uten organisasjonsnummer"));

                organisasjoner.add(mapTilOrganisasjon(utbetalingListe, jsonOrganisasjon, utbetalinger.get(0)));
            });
            skattbarInntektOgForskuddstrekkListe.add(new SkattbarInntektOgForskuddstrekk()
                    .withOrganisasjoner(organisasjoner));
        }

        Collections.reverse(skattbarInntektOgForskuddstrekkListe);
        return skattbarInntektOgForskuddstrekkListe;
    }

    private Utbetaling mapTilUtbetaling(JsonOkonomiOpplysningUtbetaling jsonOkonomiOpplysningUtbetaling) {
        return new Utbetaling()
                .withTittel(jsonOkonomiOpplysningUtbetaling.getTittel())
                .withBrutto(jsonOkonomiOpplysningUtbetaling.getBrutto())
                .withForskuddstrekk(jsonOkonomiOpplysningUtbetaling.getSkattetrekk());
    }

    private Organisasjon mapTilOrganisasjon(List<Utbetaling> utbetalingListe, JsonOrganisasjon jsonOrganisasjon, JsonOkonomiOpplysningUtbetaling utbetaling) {
        return new Organisasjon().withUtbetalinger(utbetalingListe)
                .withOrganisasjonsnavn(jsonOrganisasjon.getNavn())
                .withOrgnr(jsonOrganisasjon.getOrganisasjonsnummer())
                .withFom(utbetaling.getPeriodeFom())
                .withTom(utbetaling.getPeriodeTom());
    }


    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SkattbarInntektOgForskuddstrekk {
        public List<Organisasjon> organisasjoner;

        public SkattbarInntektOgForskuddstrekk withOrganisasjoner(List<Organisasjon> organisasjoner) {
            this.organisasjoner = organisasjoner;
            return this;
        }
    }

    @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SkattbarInntektFrontend {
        public List<SkattbarInntektOgForskuddstrekk> inntektFraSkatteetaten;
        public Boolean inntektFraSkatteetatenFeilet;
        public Boolean samtykke;
        public String samtykkeTidspunkt;

        public SkattbarInntektFrontend withInntektFraSkatteetaten(List<SkattbarInntektOgForskuddstrekk> inntektFraSkatteetaten) {
            this.inntektFraSkatteetaten = inntektFraSkatteetaten;
            return this;
        }

        public SkattbarInntektFrontend withInntektFraSkatteetatenFeilet(Boolean inntektFraSkatteetatenFeilet) {
            this.inntektFraSkatteetatenFeilet = inntektFraSkatteetatenFeilet;
            return this;
        }
        public SkattbarInntektFrontend withInntektFraSkatteetatenSamtykke(Boolean samtykke, String samtykkeTidspunkt) {
            this.samtykke = samtykke;
            this.samtykkeTidspunkt = samtykkeTidspunkt;
            return this;
        }
    }
}