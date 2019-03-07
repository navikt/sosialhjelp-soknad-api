package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.FilFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToTittelDelNavn;
import static no.nav.sbl.dialogarena.rest.mappers.SoknadTypeToVedleggTypeMapper.mapVedleggTypeToSoknadTypeAndPath;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;

@Component
public class OkonomiskeOpplysningerMapper {

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    private FaktaService faktaService;

    public void addAllInntekterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
        final Optional<JsonOkonomioversiktInntekt> eksisterendeInntekt = jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .findFirst();

        if (eksisterendeInntekt.isPresent()) {
            final List<JsonOkonomioversiktInntekt> inntekter = jsonOkonomi.getOversikt().getInntekt().stream()
                    .filter(inntekt -> !inntekt.getType().equals(jsonType))
                    .collect(Collectors.toList());

            inntekter.addAll(mapToInntektList(vedleggFrontend.rader, eksisterendeInntekt.get()));
            jsonOkonomi.getOversikt().setInntekt(inntekter);
        }
    }

    public void addAllFormuerToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
        final Optional<JsonOkonomioversiktFormue> eksisterendeFormue = jsonOkonomi.getOversikt().getFormue().stream()
                .filter(formue -> formue.getType().equals(jsonType))
                .findFirst();

        if (eksisterendeFormue.isPresent()) {
            final List<JsonOkonomioversiktFormue> formuer = jsonOkonomi.getOversikt().getFormue().stream()
                    .filter(formue -> !formue.getType().equals(jsonType))
                    .collect(Collectors.toList());

            formuer.addAll(mapToFormueList(vedleggFrontend.rader, eksisterendeFormue.get()));
            jsonOkonomi.getOversikt().setFormue(formuer);
        }
    }

    public void addAllOversiktUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
        final Optional<JsonOkonomioversiktUtgift> eksisterendeOversiktUtgift = jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .findFirst();

        if (eksisterendeOversiktUtgift.isPresent()) {
            final List<JsonOkonomioversiktUtgift> utgifter = jsonOkonomi.getOversikt().getUtgift().stream()
                    .filter(utgift -> !utgift.getType().equals(jsonType))
                    .collect(Collectors.toList());

            utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, eksisterendeOversiktUtgift.get()));

            // ---------- Spesialtilfelle for boliglan. Må kjøre på nytt for å få med renter ----------
            if (jsonType.equals("boliglanAvdrag")){
                addBoliglanRenterToUtgifter(vedleggFrontend, jsonOkonomi, utgifter);
            }
            // ----------------------------------------------------------------------------------------

            jsonOkonomi.getOversikt().setUtgift(utgifter);
        }
    }

    public void addAllOpplysningUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
        final Optional<JsonOkonomiOpplysningUtgift> eksisterendeOpplysningUtgift = jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .findFirst();

        // Dersom det ikke er en eksisterende utgift er det ikke mulig for bruker å fylle ut informasjon på vedlegget.
        if (eksisterendeOpplysningUtgift.isPresent()) {
            final List<JsonOkonomiOpplysningUtgift> utgifter = jsonOkonomi.getOpplysninger().getUtgift().stream()
                    .filter(utgift -> !utgift.getType().equals(jsonType))
                    .collect(Collectors.toList());

            // Frontend må ikke sende med rader = null eller tom liste. Må heller sende med en rad med null verdier
            utgifter.addAll(mapToOppysningUtgiftList(vedleggFrontend.rader, eksisterendeOpplysningUtgift.get()));
            jsonOkonomi.getOpplysninger().setUtgift(utgifter);
        }
    }

    public void addAllUtbetalingerToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
        final Optional<JsonOkonomiOpplysningUtbetaling> eksisterendeUtbetaling = jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getType().equals(jsonType))
                .findFirst();

        if (eksisterendeUtbetaling.isPresent()) {
            final List<JsonOkonomiOpplysningUtbetaling> utbetalinger = jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                    .filter(utbetaling -> !utbetaling.getType().equals(jsonType))
                    .collect(Collectors.toList());

            utbetalinger.addAll(mapToUtbetalingList(vedleggFrontend.rader, eksisterendeUtbetaling.get()));
            jsonOkonomi.getOpplysninger().setUtbetaling(utbetalinger);
        }
    }

    private void addBoliglanRenterToUtgifter(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, List<JsonOkonomioversiktUtgift> utgifter) {
        final Optional<JsonOkonomioversiktUtgift> eksisterendeRenter = jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals("boliglanRenter"))
                .findFirst();

        if (eksisterendeRenter.isPresent()) {
            utgifter.removeAll(utgifter.stream()
                    .filter(utgift -> utgift.getType().equals("boliglanRenter"))
                    .collect(Collectors.toList()));

            utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, eksisterendeRenter.get()));
        }
    }

    public void putNettolonnOnPropertiesForJsonTypeJobb(String belopNavn, VedleggRadFrontend vedleggRad, Map<String, String> properties) {
        if (belopNavn.equals("bruttolonn")){
            properties.put("nettolonn", vedleggRad.belop.toString());
        }
    }

    public void putBeskrivelseOnRelevantTypes(SoknadTypeAndPath soknadTypeAndPath, String jsonType, VedleggRadFrontend vedleggRad, Map<String, String> properties) {
        if (jsonType.equals("annenBoutgift") || jsonType.equals("barnFritidsaktiviteter") ||
                jsonType.equals("annenBarneutgift") ||
                (jsonType.equals("annen") && soknadTypeAndPath.getPath().equals("utbetaling")) ||
                (jsonType.equals("annen") && soknadTypeAndPath.getPath().equals("opplysningerUtgift"))){
            properties.put(jsonTypeToTittelDelNavn.get(jsonType), vedleggRad.beskrivelse);
        }
    }

    public void makeFaktumListEqualSizeToFrontendRader(VedleggFrontend vedleggFrontend, List<Faktum> fakta, String behandlingsId) {
        final int sizeDiff = vedleggFrontend.rader.size() - fakta.size();
        if (sizeDiff > 0){
            Iterator<Long> faktumIder = repository.hentLedigeFaktumIder(sizeDiff).iterator();
            for (int i = 0; i < sizeDiff; i++){
                final Faktum faktum = new Faktum()
                        .medFaktumId(faktumIder.next())
                        .medParrentFaktumId(fakta.get(0).getParrentFaktum())
                        .medKey(fakta.get(0).getKey())
                        .medType(BRUKERREGISTRERT)
                        .medSoknadId(fakta.get(0).getSoknadId());
                faktaService.opprettBrukerFaktum(behandlingsId, faktum);
                fakta.add(faktum);
            }
        } else if (sizeDiff < 0){
            for (int i = 0; i < -sizeDiff; i++){
                faktaService.slettBrukerFaktum(fakta.get(fakta.size() - 1).getFaktumId());
                fakta.remove(fakta.size() - 1);
            }
        }
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
                .withBelop(rad.belop)
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

    public VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi, List<OpplastetVedlegg> opplastedeVedlegg) {
        final List<FilFrontend> filer = vedlegg.getFiler().stream().map(fil -> {
            final OpplastetVedlegg opplastetVedlegg = opplastedeVedlegg.stream().filter(oVedlegg -> oVedlegg.getFilnavn().equals(fil.getFilnavn())).findFirst().get();
            return new FilFrontend().withFilNavn(fil.getFilnavn()).withUuid(opplastetVedlegg.getUuid());
        }).collect(Collectors.toList());

        final List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedlegg.getType(), vedlegg.getTilleggsinfo());

        return new VedleggFrontend().withType(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo())
                .withGruppe(getGruppe(vedlegg.getType(), vedlegg.getTilleggsinfo()))
                .withRader(rader)
                .withVedleggStatus(vedlegg.getStatus())
                .withFiler(filer);
    }

    private List<VedleggRadFrontend> getRader(JsonOkonomi jsonOkonomi, String type, String tilleggsinfo) {
        if (isTypeWithoutRader(type, tilleggsinfo)) return Collections.emptyList();

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
        return jsonOkonomi.getOpplysninger().getUtbetaling().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .map(this::getRadFromUtbetaling).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOpplysningerUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getInntekt().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt-> inntekt.getType().equals(jsonType))
                .map(inntekt -> getRadFromInntekt(inntekt, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOversiktUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getFormue().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getFormue().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(formue -> new VedleggRadFrontend()
                        .withBelop(formue.getBelop()))
                .collect(Collectors.toList());
    }

    private VedleggRadFrontend getRadFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
        if (utbetaling.getBelop() != null){
            return new VedleggRadFrontend().withBelop(utbetaling.getBelop());
        } else if (utbetaling.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getBrutto())));
        } else if (utbetaling.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getNetto())));
        }
        return new VedleggRadFrontend();
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
        return new VedleggRadFrontend();
    }

    private VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String jsonType) {
        if (jsonType.equals("boliglanAvdrag")){
            return new VedleggRadFrontend().withAvdrag(utgift.getBelop());
        } else if (jsonType.equals("boliglanRenter")){
            return new VedleggRadFrontend().withRenter(utgift.getBelop());
        }
        return new VedleggRadFrontend().withBelop(utgift.getBelop());
    }

    private String getGruppe(String type, String tilleggsinfo) {
        final String gruppe = getGruppeFromTypesWithoutRader(type, tilleggsinfo);
        if (gruppe != null) return gruppe;

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
        } else if (tilleggsinfo.equals("vedtak") && type.equals("bostotte")){
            return "inntekt";
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

    private String getGruppeFromTypesWithoutRader(String type, String tilleggsinfo) {
        if (type.equals("samvarsavtale") && tilleggsinfo.equals("barn")){
            return "familie";
        } else if (type.equals("husleiekontrakt")){
            return "bosituasjon";
        } else if (type.equals("skattemelding") && tilleggsinfo.equals("skattemelding")){
            return "generelle vedlegg";
        }
        return null;
    }

    private boolean isTypeWithoutRader(String type, String tilleggsinfo) {
        if (type.equals("samvarsavtale") && tilleggsinfo.equals("barn") ||
                type.equals("husleiekontrakt") ||
                type.equals("skattemelding") && tilleggsinfo.equals("skattemelding")){
            return true;
        }
        return false;
    }
}
