package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.FilFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.rest.mappers.OkonomiskGruppeMapper.getGruppe;
import static no.nav.sbl.dialogarena.rest.mappers.VedleggTypeToSoknadTypeMapper.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToTittelDelNavn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addUtgiftIfNotPresentInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.removeUtgiftIfPresentInOpplysninger;

public class OkonomiskeOpplysningerMapper {

    public static void addAllInntekterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String soknadType) {
        final Optional<JsonOkonomioversiktInntekt> eksisterendeInntekt = jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt -> inntekt.getType().equals(soknadType))
                .findFirst();

        if (eksisterendeInntekt.isPresent()) {
            final List<JsonOkonomioversiktInntekt> inntekter = jsonOkonomi.getOversikt().getInntekt().stream()
                    .filter(inntekt -> !inntekt.getType().equals(soknadType))
                    .collect(Collectors.toList());

            inntekter.addAll(mapToInntektList(vedleggFrontend.rader, eksisterendeInntekt.get()));
            jsonOkonomi.getOversikt().setInntekt(inntekter);
        }
    }

    public static void addAllFormuerToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String soknadType) {
        final Optional<JsonOkonomioversiktFormue> eksisterendeFormue = jsonOkonomi.getOversikt().getFormue().stream()
                .filter(formue -> formue.getType().equals(soknadType))
                .findFirst();

        if (eksisterendeFormue.isPresent()) {
            final List<JsonOkonomioversiktFormue> formuer = jsonOkonomi.getOversikt().getFormue().stream()
                    .filter(formue -> !formue.getType().equals(soknadType))
                    .collect(Collectors.toList());

            formuer.addAll(mapToFormueList(vedleggFrontend.rader, eksisterendeFormue.get()));
            jsonOkonomi.getOversikt().setFormue(formuer);
        }
    }

    public static void addAllOversiktUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String soknadType) {
        final Optional<JsonOkonomioversiktUtgift> eksisterendeOversiktUtgift = jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .findFirst();

        if (eksisterendeOversiktUtgift.isPresent()) {
            final List<JsonOkonomioversiktUtgift> utgifter = jsonOkonomi.getOversikt().getUtgift().stream()
                    .filter(utgift -> !utgift.getType().equals(soknadType))
                    .collect(Collectors.toList());

            utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, eksisterendeOversiktUtgift.get()));

            // ---------- Spesialtilfelle for boliglan. Må kjøre på nytt for å få med renter ----------
            if (soknadType.equals("boliglanAvdrag")){
                addBoliglanRenterToUtgifter(vedleggFrontend, jsonOkonomi, utgifter);
            }
            // ----------------------------------------------------------------------------------------

            jsonOkonomi.getOversikt().setUtgift(utgifter);
        }
    }

    public static void addAllOpplysningUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String soknadType) {
        Optional<JsonOkonomiOpplysningUtgift> eksisterendeOpplysningUtgift = jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .findFirst();

        if (vedleggFrontend.type.equals("annet|annet")){
            eksisterendeOpplysningUtgift = Optional.of(new JsonOkonomiOpplysningUtgift().withType("annen").withTittel("Annen (brukerangitt): "));
            final List<JsonOkonomiOpplysningUtgift> utgifter = jsonOkonomi.getOpplysninger().getUtgift();
            if (checkIfTypeAnnetAnnetShouldBeRemoved(vedleggFrontend)){
                removeUtgiftIfPresentInOpplysninger(utgifter, soknadType);
                return;
            } else {
                addUtgiftIfNotPresentInOpplysninger(utgifter, soknadType, eksisterendeOpplysningUtgift.get().getTittel());
            }
        }

        // Dersom det ikke er en eksisterende utgift er det ikke mulig for bruker å fylle ut informasjon på vedlegget.
        if (eksisterendeOpplysningUtgift.isPresent()) {
            final List<JsonOkonomiOpplysningUtgift> utgifter = jsonOkonomi.getOpplysninger().getUtgift().stream()
                    .filter(utgift -> !utgift.getType().equals(soknadType))
                    .collect(Collectors.toList());

            // Frontend må ikke sende med rader = null eller tom liste. Må heller sende med en rad med null verdier
            utgifter.addAll(mapToOppysningUtgiftList(vedleggFrontend.rader, eksisterendeOpplysningUtgift.get()));
            jsonOkonomi.getOpplysninger().setUtgift(utgifter);
        }
    }

    public static boolean checkIfTypeAnnetAnnetShouldBeRemoved(VedleggFrontend vedleggFrontend) {
        return vedleggFrontend.rader.size() == 1 && vedleggFrontend.rader.get(0).belop == null &&
                StringUtils.isEmpty(vedleggFrontend.rader.get(0).beskrivelse);
    }

    public static void addAllUtbetalingerToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String soknadType) {
        final Optional<JsonOkonomiOpplysningUtbetaling> eksisterendeUtbetaling = jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getType().equals(soknadType))
                .findFirst();

        if (eksisterendeUtbetaling.isPresent()) {
            final List<JsonOkonomiOpplysningUtbetaling> utbetalinger = jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                    .filter(utbetaling -> !utbetaling.getType().equals(soknadType))
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

    public static void putBeskrivelseOnRelevantTypes(String soknadPath, String soknadType, VedleggRadFrontend vedleggRad, Map<String, String> properties) {
        if (soknadType.equals("annenBoutgift") || soknadType.equals("barnFritidsaktiviteter") || soknadType.equals("annenBarneutgift") ||
                (soknadType.equals("annen") && soknadPath.equals("opplysningerUtgift"))){
            properties.put(soknadTypeToTittelDelNavn.get(soknadType), vedleggRad.beskrivelse);
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
                .withBrutto(rad.brutto != null ? rad.brutto : rad.belop)
                .withNetto(rad.netto != null ? rad.netto : rad.belop)
                .withOverstyrtAvBruker(false);
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
                .withBrutto(rad.belop != null ? Double.valueOf(rad.belop) : null)
                .withNetto(rad.belop != null ? Double.valueOf(rad.belop) : null)
                .withOverstyrtAvBruker(false);
    }

    private static List<JsonOkonomioversiktFormue> mapToFormueList(List<VedleggRadFrontend> rader, JsonOkonomioversiktFormue eksisterendeFormue) {
        return rader.stream().map(rad -> mapToFormue(rad, eksisterendeFormue)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktFormue mapToFormue(VedleggRadFrontend radFrontend, JsonOkonomioversiktFormue eksisterendeFormue) {
        return new JsonOkonomioversiktFormue().withKilde(JsonKilde.BRUKER)
                .withType(eksisterendeFormue.getType())
                .withTittel(eksisterendeFormue.getTittel())
                .withBelop(radFrontend.belop)
                .withOverstyrtAvBruker(false);
    }

    private static List<JsonOkonomioversiktUtgift> mapToOversiktUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOversiktUtgift(rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private static JsonOkonomioversiktUtgift mapToOversiktUtgift(VedleggRadFrontend radFrontend, JsonOkonomioversiktUtgift eksisterendeUtgift) {
        final String tittel = eksisterendeUtgift.getTittel();
        final String typetittel = getTypetittel(tittel);
        final String type = eksisterendeUtgift.getType();

        return new JsonOkonomioversiktUtgift().withKilde(JsonKilde.BRUKER)
                .withType(type)
                .withTittel(getTittelWithBeskrivelse(typetittel, radFrontend.beskrivelse))
                .withBelop(type.equals("boliglanAvdrag") ? radFrontend.avdrag :
                        type.equals("boliglanRenter") ? radFrontend.renter : radFrontend.belop)
                .withOverstyrtAvBruker(false);
    }

    private static String getTittelWithBeskrivelse(String typetittel, String beskrivelse) {
        return beskrivelse != null ? typetittel + beskrivelse : typetittel;
    }

    private static String getTypetittel(String tittel) {
        return !tittel.contains(":") ? tittel : tittel.substring(0, tittel.indexOf(":") + 2);
    }

    private static List<JsonOkonomiOpplysningUtgift> mapToOppysningUtgiftList(List<VedleggRadFrontend> rader, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        return rader.stream().map(rad -> mapToOppysningUtgift(rad, eksisterendeUtgift)).collect(Collectors.toList());
    }

    private static JsonOkonomiOpplysningUtgift mapToOppysningUtgift(VedleggRadFrontend radFrontend, JsonOkonomiOpplysningUtgift eksisterendeUtgift) {
        final String tittel = eksisterendeUtgift.getTittel();
        final String typetittel = getTypetittel(tittel);
        final String type = eksisterendeUtgift.getType();

        return new JsonOkonomiOpplysningUtgift().withKilde(JsonKilde.BRUKER)
                .withType(type)
                .withTittel(getTittelWithBeskrivelse(typetittel, radFrontend.beskrivelse))
                .withBelop(radFrontend.belop)
                .withOverstyrtAvBruker(false);
    }

    public static VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi, List<OpplastetVedlegg> opplastedeVedlegg) {
        final List<FilFrontend> filer = vedlegg.getFiler().stream().map(fil -> {
            final OpplastetVedlegg opplastetVedlegg = opplastedeVedlegg.stream().filter(oVedlegg -> oVedlegg.getFilnavn().equals(fil.getFilnavn()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Vedlegget finnes ikke"));
            return new FilFrontend().withFilNavn(fil.getFilnavn()).withUuid(opplastetVedlegg.getUuid());
        }).collect(Collectors.toList());

        final String vedleggType = vedlegg.getType() + "|" + vedlegg.getTilleggsinfo();

        final List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedleggType);

        return new VedleggFrontend().withType(vedleggType)
                .withGruppe(getGruppe(vedleggType))
                .withRader(rader)
                .withVedleggStatus(vedlegg.getStatus())
                .withFiler(filer);
    }

    private static List<VedleggRadFrontend> getRader(JsonOkonomi jsonOkonomi, String vedleggType) {
        if (!isInSoknadJson(vedleggType)) return Collections.emptyList();

        final String soknadType = vedleggTypeToSoknadType.get(vedleggType);
        final String soknadPath = getSoknadPath(vedleggType);

        // Spesialtilfelle for avdrag og renter
        if (soknadType.equals("boliglanAvdrag")){
            return getRadListWithAvdragAndRenter(jsonOkonomi);
        }

        switch (soknadPath){
            case "utbetaling":
                return getRadListFromUtbetaling(jsonOkonomi, soknadType);
            case "opplysningerUtgift":
                final List<VedleggRadFrontend> radList = getRadListFromOpplysningerUtgift(jsonOkonomi, soknadType);
                if (radList.isEmpty() && soknadType.equals("annen")){
                    return Collections.singletonList(new VedleggRadFrontend());
                } else {
                    return radList;
                }
            case "oversiktUtgift":
                return getRadListFromOversiktUtgift(jsonOkonomi, soknadType);
            case "formue":
                return getRadListFromFormue(jsonOkonomi, soknadType);
            case "inntekt":
                return getRadListFromInntekt(jsonOkonomi, soknadType);
        }

        return null;
    }

    private static List<VedleggRadFrontend> getRadListWithAvdragAndRenter(JsonOkonomi jsonOkonomi) {
        final List<VedleggRadFrontend> avdragRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanAvdrag");
        final List<VedleggRadFrontend> renterRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanRenter");

        if (avdragRad != null){
            for (int i = 0; i < avdragRad.size(); i++){
                avdragRad.get(i).withRenter(renterRad.get(i).renter);
            }
        }

        return avdragRad;
    }

    private static List<VedleggRadFrontend> getRadListFromUtbetaling(JsonOkonomi jsonOkonomi, String soknadType) {
        return jsonOkonomi.getOpplysninger().getUtbetaling().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(inntekt -> inntekt.getType().equals(soknadType))
                .map(OkonomiskeOpplysningerMapper::getRadFromUtbetaling).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String soknadType) {
        return jsonOkonomi.getOpplysninger().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .map(utgift -> getRadFromOpplysningerUtgift(utgift, soknadType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String soknadType) {
        return jsonOkonomi.getOversikt().getInntekt().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt-> inntekt.getType().equals(soknadType))
                .map(inntekt -> getRadFromInntekt(inntekt, soknadType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String soknadType) {
        return jsonOkonomi.getOversikt().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .map(utgift -> getRadFromOversiktUtgift(utgift, soknadType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String soknadType) {
        return jsonOkonomi.getOversikt().getFormue().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getFormue().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .map(formue -> new VedleggRadFrontend()
                        .withBelop(formue.getBelop()))
                .collect(Collectors.toList());
    }

    private static VedleggRadFrontend getRadFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
        if (utbetaling.getBelop() != null){
            return new VedleggRadFrontend().withBelop(utbetaling.getBelop());
        } else if (utbetaling.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getBrutto())));
        } else if (utbetaling.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getNetto())));
        }
        return new VedleggRadFrontend();
    }

    private static VedleggRadFrontend getRadFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift, String soknadType) {
        switch (soknadType){
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

    private static VedleggRadFrontend getRadFromInntekt(JsonOkonomioversiktInntekt inntekt, String soknadType) {
        if (soknadType.equals("jobb")){
            return new VedleggRadFrontend()
                    .withBrutto(inntekt.getBrutto())
                    .withNetto(inntekt.getNetto());
        }
        if (inntekt.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(inntekt.getBrutto());
        } else if (inntekt.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(inntekt.getNetto());
        }
        return new VedleggRadFrontend();
    }

    private static VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String soknadType) {
        if (soknadType.equals("boliglanAvdrag")){
            return new VedleggRadFrontend().withAvdrag(utgift.getBelop());
        } else if (soknadType.equals("boliglanRenter")){
            return new VedleggRadFrontend().withRenter(utgift.getBelop());
        }
        return new VedleggRadFrontend().withBelop(utgift.getBelop());
    }
}
