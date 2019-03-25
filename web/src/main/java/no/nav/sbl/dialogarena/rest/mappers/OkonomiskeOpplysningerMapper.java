package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;

import java.util.*;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToTittelDelNavn;
import static no.nav.sbl.dialogarena.rest.mappers.SoknadTypeToVedleggTypeMapper.mapVedleggTypeToSoknadTypeAndPath;

public class OkonomiskeOpplysningerMapper {

    public static void addAllInntekterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
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

    public static void addAllFormuerToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
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

    public static void addAllOversiktUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
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

    public static void addAllOpplysningUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
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

    public static void addAllUtbetalingerToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String jsonType) {
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

    private static void addBoliglanRenterToUtgifter(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, List<JsonOkonomioversiktUtgift> utgifter) {
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

    public static void putNettolonnOnPropertiesForJsonTypeJobb(String belopNavn, RadAlleFelter vedleggRad, Map<String, String> properties) {
        if (belopNavn.equals("bruttolonn")){
            properties.put("nettolonn", vedleggRad.belop.toString());
        }
    }

    public static void putBeskrivelseOnRelevantTypes(SoknadTypeAndPath soknadTypeAndPath, String jsonType, RadAlleFelter vedleggRad, Map<String, String> properties) {
        if (jsonType.equals("annenBoutgift") || jsonType.equals("barnFritidsaktiviteter") ||
                jsonType.equals("annenBarneutgift") ||
                (jsonType.equals("annen") && soknadTypeAndPath.getPath().equals("utbetaling")) ||
                (jsonType.equals("annen") && soknadTypeAndPath.getPath().equals("opplysningerUtgift"))){
            properties.put(jsonTypeToTittelDelNavn.get(jsonType), vedleggRad.beskrivelse);
        }
    }

    private static List<JsonOkonomioversiktInntekt> mapToInntektList(List<VedleggRadFrontend> rader, JsonOkonomioversiktInntekt eksisterendeInntekt) {
        return rader.stream().map(rad -> mapToInntekt((RadAlleFelter) rad, eksisterendeInntekt)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktInntekt mapToInntekt(RadAlleFelter rad, JsonOkonomioversiktInntekt eksisterendeInntekt) {
        return new JsonOkonomioversiktInntekt()
                .withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeInntekt.getType())
                .withTittel(eksisterendeInntekt.getTittel())
                .withBrutto(rad.brutto != null ? rad.brutto : rad.belop)
                .withNetto(rad.netto != null ? rad.netto : rad.belop);
    }

    private static List<JsonOkonomiOpplysningUtbetaling> mapToUtbetalingList(List<VedleggRadFrontend> rader, JsonOkonomiOpplysningUtbetaling eksisterendeUtbetaling) {
        return rader.stream().map(rad -> mapToUtbetaling((RadAlleFelter) rad, eksisterendeUtbetaling)).collect(Collectors.toList());
    }

    private static JsonOkonomiOpplysningUtbetaling mapToUtbetaling(RadAlleFelter rad, JsonOkonomiOpplysningUtbetaling eksisterendeUtbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeUtbetaling.getType())
                .withTittel(eksisterendeUtbetaling.getTittel())
                .withBelop(rad.belop)
                .withBrutto(Double.valueOf(rad.belop))
                .withNetto(Double.valueOf(rad.belop));
    }

    private static List<JsonOkonomioversiktFormue> mapToFormueList(List<VedleggRadFrontend> rader, JsonOkonomioversiktFormue eksisterendeFormue) {
        return rader.stream().map(rad -> mapToFormue((RadAlleFelter) rad, eksisterendeFormue)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktFormue mapToFormue(RadAlleFelter radFrontend, JsonOkonomioversiktFormue eksisterendeFormue) {
        return new JsonOkonomioversiktFormue().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeFormue.getType())
                .withTittel(eksisterendeFormue.getTittel())
                .withBelop(radFrontend.belop);
    }

    private static List<JsonOkonomioversiktUtgift> mapToOversiktUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOversiktUtgift((RadAlleFelter) rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktUtgift mapToOversiktUtgift(RadAlleFelter radFrontend, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        final String tittel = eksisterendeUtgift.getTittel();
        final String typetittel = !tittel.contains(":") ? tittel : tittel.substring(0, tittel.indexOf(":") + 2);
        final String type = eksisterendeUtgift.getType();

        return new JsonOkonomioversiktUtgift().withKilde(JsonKilde.BRUKER)
                .withType(type)
                .withTittel(radFrontend.beskrivelse != null ? typetittel + radFrontend.beskrivelse : typetittel)
                .withBelop(type.equals("boliglanAvdrag") ? radFrontend.avdrag :
                        type.equals("boliglanRenter") ? radFrontend.renter : radFrontend.belop);
    }

    private static List<JsonOkonomiOpplysningUtgift> mapToOppysningUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOppysningUtgift((RadAlleFelter) rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private static JsonOkonomiOpplysningUtgift mapToOppysningUtgift(RadAlleFelter radFrontend, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        return new JsonOkonomiOpplysningUtgift().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeUtgift.getType())
                .withTittel(eksisterendeUtgift.getTittel())
                .withBelop(radFrontend.belop);
    }

    public static VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi, List<OpplastetVedlegg> opplastedeVedlegg) {
        final List<FilFrontend> filer = vedlegg.getFiler().stream().map(fil -> {
            final OpplastetVedlegg opplastetVedlegg = opplastedeVedlegg.stream().filter(oVedlegg -> oVedlegg.getFilnavn().equals(fil.getFilnavn()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Vedlegget finnes ikke"));
            return new FilFrontend().withFilNavn(fil.getFilnavn()).withUuid(opplastetVedlegg.getUuid());
        }).collect(Collectors.toList());

        final List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedlegg.getType(), vedlegg.getTilleggsinfo());

        return new VedleggFrontend().withType(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo())
                .withGruppe(getGruppe(vedlegg.getType(), vedlegg.getTilleggsinfo()))
                .withRader(rader)
                .withVedleggStatus(vedlegg.getStatus())
                .withFiler(filer);
    }

    private static List<VedleggRadFrontend> getRader(JsonOkonomi jsonOkonomi, String type, String tilleggsinfo) {
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

    private static List<VedleggRadFrontend> getRadListWithAvdragAndRenter(JsonOkonomi jsonOkonomi) {
        final List<VedleggRadFrontend> avdragRader = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanAvdrag");
        final List<VedleggRadFrontend> renterRader = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanRenter");
        final List<VedleggRadFrontend> avdragRenterRader = new ArrayList<>();

        if (avdragRader != null){
            for (int i = 0; i < avdragRader.size(); i++){
                RadAvdragRenter avdragRad = (RadAvdragRenter) avdragRader.get(i);
                RadAvdragRenter renterRad = (RadAvdragRenter) renterRader.get(i);
                avdragRenterRader.add(new RadAvdragRenter()
                        .withAvdrag(avdragRad.avdrag)
                        .withRenter(renterRad.renter));
            }
        }

        return avdragRenterRader;
    }

    private static List<VedleggRadFrontend> getRadListFromUtbetaling(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtbetaling().isEmpty() ? Collections.emptyList() :
                jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .map(OkonomiskeOpplysningerMapper::getRadFromUtbetaling).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtgift().isEmpty() ? Collections.emptyList() :
                jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOpplysningerUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getInntekt().isEmpty() ? Collections.emptyList() :
                jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt-> inntekt.getType().equals(jsonType))
                .map(inntekt -> getRadFromInntekt(inntekt, jsonType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getUtgift().isEmpty() ? Collections.emptyList() :
                jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOversiktUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getFormue().isEmpty() ? Collections.emptyList() :
                jsonOkonomi.getOversikt().getFormue().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(formue -> new RadBelop()
                        .withBelop(formue.getBelop()))
                .collect(Collectors.toList());
    }

    private static VedleggRadFrontend getRadFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
        if (utbetaling.getBelop() != null){
            return new RadBelop().withBelop(utbetaling.getBelop());
        } else if (utbetaling.getBrutto() != null){
            return new RadBruttoNetto()
                    .withBrutto(new Integer(String.valueOf(utbetaling.getBrutto())))
                    .withNetto(new Integer(String.valueOf(utbetaling.getNetto())));
        }
        return new RadBelop();
    }

    private static VedleggRadFrontend getRadFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift, String jsonType) {
        switch (jsonType){
            case "annen":
            case "annenBarneutgift":
            case "annenBoutgift":
            case "barnFritidsaktiviteter":
                return new RadBeskrivelseBelop().withBelop(utgift.getBelop())
                        .withBeskrivelse(utgift.getTittel().substring(utgift.getTittel().indexOf(":") + 2));
            default:
                return new RadBelop().withBelop(utgift.getBelop());
        }
    }

    private static VedleggRadFrontend getRadFromInntekt(JsonOkonomioversiktInntekt inntekt, String jsonType) {
        if (jsonType.equals("jobb")){
            return new RadBruttoNetto()
                    .withBrutto(inntekt.getBrutto())
                    .withNetto(inntekt.getNetto());
        }
        if (inntekt.getBrutto() != null){
            return new RadBelop().withBelop(inntekt.getBrutto());
        } else if (inntekt.getNetto() != null) {
            return new RadBelop().withBelop(inntekt.getNetto());
        }
        return new RadBelop();
    }

    private static VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String jsonType) {
        if (jsonType.equals("boliglanAvdrag")){
            return new RadAvdragRenter().withAvdrag(utgift.getBelop());
        } else if (jsonType.equals("boliglanRenter")){
            return new RadAvdragRenter().withRenter(utgift.getBelop());
        }
        return new RadBelop().withBelop(utgift.getBelop());
    }

    private static String getGruppe(String type, String tilleggsinfo) {
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
                if (type.equals("annet") && tilleggsinfo.equals("annet")){
                    return "andre utgifter";
                }
                return "utgifter";
            }
        }

        return null;
    }

    private static String getGruppeFromTypesWithoutRader(String type, String tilleggsinfo) {
        if (type.equals("samvarsavtale") && tilleggsinfo.equals("barn")){
            return "familie";
        } else if (type.equals("husleiekontrakt")){
            return "bosituasjon";
        } else if (type.equals("skattemelding") && tilleggsinfo.equals("skattemelding")){
            return "generelle vedlegg";
        }
        return null;
    }

    private static boolean isTypeWithoutRader(String type, String tilleggsinfo) {
        if (type.equals("samvarsavtale") && tilleggsinfo.equals("barn") ||
                type.equals("husleiekontrakt") ||
                type.equals("skattemelding") && tilleggsinfo.equals("skattemelding")){
            return true;
        }
        return false;
    }
}
