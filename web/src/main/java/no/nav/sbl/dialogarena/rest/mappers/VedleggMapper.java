package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.EttersendtVedlegg;
import no.nav.sbl.dialogarena.rest.ressurser.FilFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;

import java.util.*;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.rest.mappers.SoknadTypeToVedleggTypeMapper.mapVedleggTypeToSoknadTypeAndPath;

public class VedleggMapper {

    public static VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi, List<OpplastetVedlegg> opplastedeVedlegg) {
        final List<FilFrontend> filer = mapJsonFilerAndOpplastedeVedleggToFilerFrontend(vedlegg.getFiler(), opplastedeVedlegg);
        final List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedlegg.getType(), vedlegg.getTilleggsinfo());

        return new VedleggFrontend().withType(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo())
                .withGruppe(getGruppe(vedlegg.getType(), vedlegg.getTilleggsinfo()))
                .withRader(rader)
                .withVedleggStatus(vedlegg.getStatus())
                .withFiler(filer);
    }

    public static List<FilFrontend> mapJsonFilerAndOpplastedeVedleggToFilerFrontend(List<JsonFiler> filer, List<OpplastetVedlegg> opplastedeVedlegg) {
        return filer.stream().map(fil -> {
            final OpplastetVedlegg opplastetVedlegg = opplastedeVedlegg.stream().filter(oVedlegg -> oVedlegg.getFilnavn().equals(fil.getFilnavn()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Vedlegget finnes ikke"));
            return new FilFrontend().withFilNavn(fil.getFilnavn()).withUuid(opplastetVedlegg.getUuid());
        }).collect(Collectors.toList());
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
        final List<VedleggRadFrontend> avdragRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanAvdrag");
        final List<VedleggRadFrontend> renterRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanRenter");

        if (avdragRad != null){
            for (int i = 0; i < avdragRad.size(); i++){
                avdragRad.get(i).withRenter(renterRad.get(i).renter);
            }
        }

        return avdragRad;
    }

    private static List<VedleggRadFrontend> getRadListFromUtbetaling(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtbetaling().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                        .filter(inntekt -> inntekt.getType().equals(jsonType))
                        .map(VedleggMapper::getRadFromUtbetaling).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOpplysninger().getUtgift().stream()
                        .filter(utgift -> utgift.getType().equals(jsonType))
                        .map(utgift -> getRadFromOpplysningerUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getInntekt().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getInntekt().stream()
                        .filter(inntekt-> inntekt.getType().equals(jsonType))
                        .map(inntekt -> getRadFromInntekt(inntekt, jsonType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getUtgift().stream()
                        .filter(utgift -> utgift.getType().equals(jsonType))
                        .map(utgift -> getRadFromOversiktUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private static List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getFormue().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
                jsonOkonomi.getOversikt().getFormue().stream()
                        .filter(utgift -> utgift.getType().equals(jsonType))
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

    private static VedleggRadFrontend getRadFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift, String jsonType) {
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

    private static VedleggRadFrontend getRadFromInntekt(JsonOkonomioversiktInntekt inntekt, String jsonType) {
        if (inntekt.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(inntekt.getBrutto());
        } else if (inntekt.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(inntekt.getNetto());
        }
        return new VedleggRadFrontend();
    }

    private static VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String jsonType) {
        if (jsonType.equals("boliglanAvdrag")){
            return new VedleggRadFrontend().withAvdrag(utgift.getBelop());
        } else if (jsonType.equals("boliglanRenter")){
            return new VedleggRadFrontend().withRenter(utgift.getBelop());
        }
        return new VedleggRadFrontend().withBelop(utgift.getBelop());
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

    public static List<EttersendtVedlegg> mapVedleggToSortedListOfEttersendteVedlegg(List<OpplastetVedlegg> opplastedeVedlegg, List<JsonVedlegg> originaleVedlegg) {
        final SortedMap<String, EttersendtVedlegg> ettersendteVedlegg = new TreeMap<>(sortAlphabeticallyAndPutTypeAnnetLast());

        originaleVedlegg.forEach(vedlegg -> {
            String sammensattNavn = vedlegg.getType() + "|" + vedlegg.getTilleggsinfo();

            if (!ettersendteVedlegg.containsKey(sammensattNavn)) {
                List<FilFrontend> filerFrontend = new ArrayList<>();
                if (vedlegg.getStatus().equals("LastetOpp")){
                    final List<JsonFiler> filer = vedlegg.getFiler();
                    filerFrontend = mapJsonFilerAndOpplastedeVedleggToFilerFrontend(filer, opplastedeVedlegg);
                }

                ettersendteVedlegg.put(sammensattNavn, new EttersendtVedlegg()
                        .withType(sammensattNavn)
                        .withVedleggStatus(vedlegg.getStatus())
                        .withFiler(filerFrontend));
            }
        });

        return new ArrayList<>(ettersendteVedlegg.values());
    }

    private static Comparator<String> sortAlphabeticallyAndPutTypeAnnetLast() {
        return (o1, o2) -> {
            if (o1.equals(o2)) {
                return 0;
            } else if (o1.equals("annet|annet")) {
                return 1;
            } else if (o2.equals("annet|annet")) {
                return -1;
            }
            return o1.compareTo(o2);
        };
    }
}
