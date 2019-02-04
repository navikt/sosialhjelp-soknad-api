package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadTypeAndPath;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.ressurser.SoknadTypeToVedleggTypeMapper.mapVedleggTypeToSoknadTypeAndPath;

@Controller
@Path("/soknader/{behandlingsId}/okonomiskeOpplysninger")
@Timed
@Produces(APPLICATION_JSON)
public class OkonomiskeOpplysningerRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final List<JsonVedlegg> jsonVedleggs = soknad.getVedlegg().getVedlegg();
        final JsonOkonomi jsonOkonomi = soknad.getSoknad().getData().getOkonomi();

        if (jsonVedleggs != null && !jsonVedleggs.isEmpty()){
            return new VedleggFrontends().withOkonomiskeOpplysninger(jsonVedleggs.stream()
                    .map(vedlegg -> mapToVedleggFrontend(vedlegg, jsonOkonomi)).collect(Collectors.toList()));
        }
        return new VedleggFrontends();
    }

    @PUT
    public void updateOkonomiskOpplysning(@PathParam("behandlingsId") String behandlingsId, VedleggFrontend vedleggFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, vedleggFrontend);
        legacyUpdate(behandlingsId, vedleggFrontend);
    }

    private void update(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final List<JsonVedlegg> jsonVedleggs = soknad.getJsonInternalSoknad().getVedlegg().getVedlegg();
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        final String type = vedleggFrontend.type.substring(0, vedleggFrontend.type.indexOf("."));
        final String tilleggsinfo = vedleggFrontend.type.substring(vedleggFrontend.type.indexOf(".") + 1);

        if (vedleggFrontend.vedleggStatus != null){
            updateVedleggStatus(vedleggFrontend, jsonVedleggs, type, tilleggsinfo);
        }

        final SoknadTypeAndPath soknadTypeAndPath = mapVedleggTypeToSoknadTypeAndPath(type, tilleggsinfo);
        final String jsonType = soknadTypeAndPath.getType();

        switch (soknadTypeAndPath.getPath()){
            case "utbetaling":
                final Optional<JsonOkonomiOpplysningUtbetaling> eksisterendeUtbetaling = jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                        .filter(utbetaling -> utbetaling.getType().equals(jsonType))
                        .findFirst();

                if (eksisterendeUtbetaling.isPresent()) {
                    List<JsonOkonomiOpplysningUtbetaling> utbetalinger = jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                            .filter(utbetaling -> !utbetaling.getType().equals(jsonType))
                            .collect(Collectors.toList());

                    utbetalinger.addAll(mapToUtbetalingList(vedleggFrontend.rader, eksisterendeUtbetaling.get()));
                    jsonOkonomi.getOpplysninger().setUtbetaling(utbetalinger);
                }
                break;
            case "opplysningerUtgift":
                final Optional<JsonOkonomiOpplysningUtgift> eksisterendeOpplysningUtgift = jsonOkonomi.getOpplysninger().getUtgift().stream()
                        .filter(utgift -> utgift.getType().equals(jsonType))
                        .findFirst();

                // Dersom det ikke er en eksisterende utgift er det ikke mulig for bruker å fylle ut informasjon på vedlegget.
                if (eksisterendeOpplysningUtgift.isPresent()) {
                    List<JsonOkonomiOpplysningUtgift> utgifter = jsonOkonomi.getOpplysninger().getUtgift().stream()
                            .filter(utgift -> !utgift.getType().equals(jsonType))
                            .collect(Collectors.toList());

                    // Frontend må ikke sende med rader = null eller tom liste. Må heller sende med en rad med null verdier
                    utgifter.addAll(mapToOppysningUtgiftList(vedleggFrontend.rader, eksisterendeOpplysningUtgift.get()));
                    jsonOkonomi.getOpplysninger().setUtgift(utgifter);
                }
                break;
            case "oversiktUtgift":
                final Optional<JsonOkonomioversiktUtgift> eksisterendeOversiktUtgift = jsonOkonomi.getOversikt().getUtgift().stream()
                        .filter(utgift -> utgift.getType().equals(jsonType))
                        .findFirst();

                if (eksisterendeOversiktUtgift.isPresent()) {
                    List<JsonOkonomioversiktUtgift> utgifter = jsonOkonomi.getOversikt().getUtgift().stream()
                            .filter(utgift -> !utgift.getType().equals(jsonType))
                            .collect(Collectors.toList());

                    utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, eksisterendeOversiktUtgift.get()));

                    // ---------- Spesialtilfelle for boliglan. Må kjøre på nytt for å få med renter ----------
                    if (jsonType.equals("boliglanAvdrag")){
                        final Optional<JsonOkonomioversiktUtgift> eksisterendeRenter = jsonOkonomi.getOversikt().getUtgift().stream()
                                .filter(utgift -> utgift.getType().equals("boliglanRenter"))
                                .findFirst();

                        if (eksisterendeRenter.isPresent()) {
                            utgifter = utgifter.stream()
                                    .filter(utgift -> !utgift.getType().equals("boliglanRenter"))
                                    .collect(Collectors.toList());

                            utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, eksisterendeRenter.get()));
                        }
                    }
                    // ----------------------------------------------------------------------------------------

                    jsonOkonomi.getOversikt().setUtgift(utgifter);
                }
                break;
            case "formue":
                final Optional<JsonOkonomioversiktFormue> eksisterendeFormue = jsonOkonomi.getOversikt().getFormue().stream()
                        .filter(formue -> formue.getType().equals(jsonType))
                        .findFirst();

                if (eksisterendeFormue.isPresent()) {
                    List<JsonOkonomioversiktFormue> formuer = jsonOkonomi.getOversikt().getFormue().stream()
                            .filter(formue -> !formue.getType().equals(jsonType))
                            .collect(Collectors.toList());

                    formuer.addAll(mapToFormueList(vedleggFrontend.rader, eksisterendeFormue.get()));
                    jsonOkonomi.getOversikt().setFormue(formuer);
                }
                break;
            case "inntekt":
                final Optional<JsonOkonomioversiktInntekt> eksisterendeInntekt = jsonOkonomi.getOversikt().getInntekt().stream()
                        .filter(inntekt -> inntekt.getType().equals(jsonType))
                        .findFirst();

                if (eksisterendeInntekt.isPresent()) {
                    List<JsonOkonomioversiktInntekt> inntekter = jsonOkonomi.getOversikt().getInntekt().stream()
                            .filter(inntekt -> !inntekt.getType().equals(jsonType))
                            .collect(Collectors.toList());

                    inntekter.addAll(mapToInntektList(vedleggFrontend.rader, eksisterendeInntekt.get()));
                    jsonOkonomi.getOversikt().setInntekt(inntekter);
                }
                break;
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private List<JsonOkonomioversiktInntekt> mapToInntektList(List<VedleggRadFrontend> rader, JsonOkonomioversiktInntekt eksisterendeInntekt) {
        return rader.stream().map(rad -> mapToInntekt(rad, eksisterendeInntekt)).collect(Collectors.toList());
    }

    private JsonOkonomioversiktInntekt mapToInntekt(VedleggRadFrontend rad, JsonOkonomioversiktInntekt eksisterendeInntekt) {
        return new JsonOkonomioversiktInntekt()
                .withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeInntekt.getType())
                .withTittel(eksisterendeInntekt.getTittel())
                .withBrutto(rad.belop)
                .withNetto(rad.belop);
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToUtbetalingList(List<VedleggRadFrontend> rader, JsonOkonomiOpplysningUtbetaling eksisterendeUtbetaling) {
        return rader.stream().map(rad -> mapToUtbetaling(rad, eksisterendeUtbetaling)).collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtbetaling mapToUtbetaling(VedleggRadFrontend rad, JsonOkonomiOpplysningUtbetaling eksisterendeUtbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeUtbetaling.getType())
                .withTittel(eksisterendeUtbetaling.getTittel())
                .withBelop(rad.belop) // Sjekk hvilke av disse som skal settes
                .withBrutto(Double.valueOf(rad.belop))
                .withNetto(Double.valueOf(rad.belop));
    }

    private List<JsonOkonomioversiktFormue> mapToFormueList(List<VedleggRadFrontend> rader, JsonOkonomioversiktFormue eksisterendeFormue) {
        return rader.stream().map(rad -> mapToFormue(rad, eksisterendeFormue)).collect(Collectors.toList());
    }

    private JsonOkonomioversiktFormue mapToFormue(VedleggRadFrontend radFrontend, JsonOkonomioversiktFormue eksisterendeFormue) {
        return new JsonOkonomioversiktFormue().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeFormue.getType())
                .withTittel(eksisterendeFormue.getTittel())
                .withBelop(radFrontend.belop);
    }

    private List<JsonOkonomioversiktUtgift> mapToOversiktUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOversiktUtgift(rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private JsonOkonomioversiktUtgift mapToOversiktUtgift(VedleggRadFrontend radFrontend, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        final String tittel = eksisterendeUtgift.getTittel();
        final String typetittel = !tittel.contains(":") ? tittel : tittel.substring(0, tittel.indexOf(":") + 2);
        final String type = eksisterendeUtgift.getType();

        return new JsonOkonomioversiktUtgift().withKilde(JsonKilde.BRUKER)
                .withType(type)
                .withTittel(radFrontend.beskrivelse != null ? typetittel + radFrontend.beskrivelse : typetittel)
                .withBelop(type.equals("boliglanAvdrag") ? radFrontend.avdrag :
                        type.equals("boliglanRenter") ? radFrontend.renter : radFrontend.belop);
    }

    private List<JsonOkonomiOpplysningUtgift> mapToOppysningUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOppysningUtgift(rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtgift mapToOppysningUtgift(VedleggRadFrontend radFrontend, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        return new JsonOkonomiOpplysningUtgift().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeUtgift.getType())
                .withTittel(eksisterendeUtgift.getTittel())
                .withBelop(radFrontend.belop);
    }

    private void updateVedleggStatus(VedleggFrontend vedleggFrontend, List<JsonVedlegg> jsonVedleggs, String type, String tilleggsinfo) {
        jsonVedleggs.stream()
                .filter(vedlegg -> vedlegg.getType().equals(type))
                .filter(vedlegg -> vedlegg.getTilleggsinfo().equals(tilleggsinfo))
                .findFirst().get().setStatus(vedleggFrontend.vedleggStatus);
    }

    private void legacyUpdate(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);
    }

    private VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi) {
        final List<String> filer = vedlegg.getFiler().stream().map(JsonFiler::getFilnavn)
                .collect(Collectors.toList());
        final List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedlegg.getType(), vedlegg.getTilleggsinfo());

        return new VedleggFrontend().withType(vedlegg.getType() + "." + vedlegg.getTilleggsinfo())
                .withGruppe(getGruppe(vedlegg.getType(), vedlegg.getTilleggsinfo()))
                .withRader(rader)
                .withVedleggStatus(vedlegg.getStatus())
                .withFilNavn(filer);
    }

    private List<VedleggRadFrontend> getRader(JsonOkonomi jsonOkonomi, String type, String tilleggsinfo) {
        final SoknadTypeAndPath soknadTypeAndPath = mapVedleggTypeToSoknadTypeAndPath(type, tilleggsinfo);

        // Spesialtilfelle for avdrag og renter
        if (soknadTypeAndPath.getType().equals("boliglanAvdrag")){
            return getRadListWithAvdragAndRenter(jsonOkonomi);
        }

        switch (soknadTypeAndPath.getPath()){
            case "utbetaling":
                return getRadListFromUtbetaling(jsonOkonomi, soknadTypeAndPath.getType());
            case "opplysningerUtgift":
                return getRadListFromOpplysningerUtgift(jsonOkonomi, soknadTypeAndPath.getType());
            case "oversiktUtgift":
                return getRadListFromOversiktUtgift(jsonOkonomi, soknadTypeAndPath.getType());
            case "formue":
                return getRadListFromFormue(jsonOkonomi, soknadTypeAndPath.getType());
            case "inntekt":
                return getRadListFromInntekt(jsonOkonomi, soknadTypeAndPath.getType());
        }

        return null;
    }

    private List<VedleggRadFrontend> getRadListWithAvdragAndRenter(JsonOkonomi jsonOkonomi) {
        final List<VedleggRadFrontend> avdragRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanAvdrag");
        final List<VedleggRadFrontend> renterRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanRenter");

        if (avdragRad != null){
            for (int i = 0; i < avdragRad.size(); i++){
                avdragRad.get(i).withRenter(renterRad.get(i).renter);
            }
        }

        return avdragRad;
    }

    private List<VedleggRadFrontend> getRadListFromUtbetaling(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .map(this::getRadFromUtbetaling).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOpplysningerUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt-> inntekt.getType().equals(jsonType))
                .map(inntekt -> getRadFromInntekt(inntekt, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOversiktUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getFormue().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(formue -> getRadFromFormue(formue, jsonType)).collect(Collectors.toList());
    }

    private VedleggRadFrontend getRadFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
        if (utbetaling.getBelop() != null){
            return new VedleggRadFrontend().withBelop(utbetaling.getBelop());
        } else if (utbetaling.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getBrutto())));
        } else if (utbetaling.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getNetto())));
        }
        return null;
    }

    private VedleggRadFrontend getRadFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift, String jsonType) {
        switch (jsonType){
            case "annen":
            case "annenBarneutgift":
            case "annenBoutgift":
            case "barnFritidsaktiviteter":
                return new VedleggRadFrontend().withBelop(utgift.getBelop())
                        .withBeskrivelse(utgift.getTittel().substring(utgift.getTittel().indexOf(":") + 2));
            default:
                return new VedleggRadFrontend().withBelop(utgift.getBelop());
        }
    }

    private VedleggRadFrontend getRadFromInntekt(JsonOkonomioversiktInntekt inntekt, String jsonType) {
        if (inntekt.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(inntekt.getBrutto());
        } else if (inntekt.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(inntekt.getNetto());
        }
        return null;
    }

    private VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String jsonType) {
        if (jsonType.equals("boliglanAvdrag")){
            return new VedleggRadFrontend().withAvdrag(utgift.getBelop());
        } else if (jsonType.equals("boliglanRenter")){
            return new VedleggRadFrontend().withRenter(utgift.getBelop());
        }
        return new VedleggRadFrontend().withBelop(utgift.getBelop());
    }

    private VedleggRadFrontend getRadFromFormue(JsonOkonomioversiktFormue formue, String jsonType) {
        return new VedleggRadFrontend().withBelop(formue.getBelop());
    }

    private String getGruppe(String type, String tilleggsinfo) {
        final SoknadTypeAndPath soknadTypeAndPath = mapVedleggTypeToSoknadTypeAndPath(type, tilleggsinfo);
        final String path = soknadTypeAndPath.getPath();

        if (tilleggsinfo.equals("mottar") || tilleggsinfo.equals("betaler") || tilleggsinfo.equals("barn")){
            return "familie";
        } else if (tilleggsinfo.equals("husleiekontrakt")){
            return "bosituasjon";
        } else if (tilleggsinfo.equals("arbeid") && type.equals("sluttoppgjor")){
            return "arbeid";
        } else if (tilleggsinfo.equals("vedtak") && type.equals("student")){
            return "arbeid";
        } else {
            if (path.equals("utbetaling") || path.equals("formue")){
                return "inntekt";
            }
            if (path.equals("opplysningerUtgift") || path.equals("oversiktUtgift")){
                return "utgifter";
            }
        }

        return null;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class VedleggFrontends {
        public List<VedleggFrontend> okonomiskeOpplysninger;

        public VedleggFrontends withOkonomiskeOpplysninger(List<VedleggFrontend> okonomiskeOpplysninger) {
            this.okonomiskeOpplysninger = okonomiskeOpplysninger;
            return this;
        }
    }
}
