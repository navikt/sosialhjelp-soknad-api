package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToTittelDelNavn;

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

    public static void putNettolonnOnPropertiesForJsonTypeJobb(String belopNavn, VedleggRadFrontend vedleggRad, Map<String, String> properties) {
        if (belopNavn.equals("bruttolonn")){
            properties.put("nettolonn", vedleggRad.belop.toString());
        }
    }

    public static void putBeskrivelseOnRelevantTypes(SoknadTypeAndPath soknadTypeAndPath, String jsonType, VedleggRadFrontend vedleggRad, Map<String, String> properties) {
        if (jsonType.equals("annenBoutgift") || jsonType.equals("barnFritidsaktiviteter") ||
                jsonType.equals("annenBarneutgift") ||
                (jsonType.equals("annen") && soknadTypeAndPath.getPath().equals("utbetaling")) ||
                (jsonType.equals("annen") && soknadTypeAndPath.getPath().equals("opplysningerUtgift"))){
            properties.put(jsonTypeToTittelDelNavn.get(jsonType), vedleggRad.beskrivelse);
        }
    }

    private static List<JsonOkonomioversiktInntekt> mapToInntektList(List<VedleggRadFrontend> rader, JsonOkonomioversiktInntekt eksisterendeInntekt) {
        return rader.stream().map(rad -> mapToInntekt(rad, eksisterendeInntekt)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktInntekt mapToInntekt(VedleggRadFrontend rad, JsonOkonomioversiktInntekt eksisterendeInntekt) {
        return new JsonOkonomioversiktInntekt()
                .withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeInntekt.getType())
                .withTittel(eksisterendeInntekt.getTittel())
                .withBrutto(rad.belop)
                .withNetto(rad.belop);
    }

    private static List<JsonOkonomiOpplysningUtbetaling> mapToUtbetalingList(List<VedleggRadFrontend> rader, JsonOkonomiOpplysningUtbetaling eksisterendeUtbetaling) {
        return rader.stream().map(rad -> mapToUtbetaling(rad, eksisterendeUtbetaling)).collect(Collectors.toList());
    }

    private static JsonOkonomiOpplysningUtbetaling mapToUtbetaling(VedleggRadFrontend rad, JsonOkonomiOpplysningUtbetaling eksisterendeUtbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeUtbetaling.getType())
                .withTittel(eksisterendeUtbetaling.getTittel())
                .withBelop(rad.belop)
                .withBrutto(Double.valueOf(rad.belop))
                .withNetto(Double.valueOf(rad.belop));
    }

    private static List<JsonOkonomioversiktFormue> mapToFormueList(List<VedleggRadFrontend> rader, JsonOkonomioversiktFormue eksisterendeFormue) {
        return rader.stream().map(rad -> mapToFormue(rad, eksisterendeFormue)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktFormue mapToFormue(VedleggRadFrontend radFrontend, JsonOkonomioversiktFormue eksisterendeFormue) {
        return new JsonOkonomioversiktFormue().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeFormue.getType())
                .withTittel(eksisterendeFormue.getTittel())
                .withBelop(radFrontend.belop);
    }

    private static List<JsonOkonomioversiktUtgift> mapToOversiktUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOversiktUtgift(rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktUtgift mapToOversiktUtgift(VedleggRadFrontend radFrontend, JsonOkonomioversiktUtgift eksisterendeUtgift) {
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
        return rader.stream().map(rad -> mapToOppysningUtgift(rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private static JsonOkonomiOpplysningUtgift mapToOppysningUtgift(VedleggRadFrontend radFrontend, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        return new JsonOkonomiOpplysningUtgift().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeUtgift.getType())
                .withTittel(eksisterendeUtgift.getTittel())
                .withBelop(radFrontend.belop);
    }
}
