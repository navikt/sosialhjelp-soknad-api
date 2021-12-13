//package no.nav.sosialhjelp.soknad.web.rest.mappers;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.EttersendtVedlegg;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.FilFrontend;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.VedleggFrontend;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.VedleggRadFrontend;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.SortedMap;
//import java.util.TreeMap;
//import java.util.stream.Collectors;
//
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER;
//import static no.nav.sosialhjelp.soknad.business.util.EttersendelseUtils.soknadSendtForMindreEnn30DagerSiden;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskGruppeMapper.getGruppe;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.getSoknadPath;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.isInSoknadJson;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType;
//
//public final class VedleggMapper {
//
//    private static final String ANNET_ANNET = "annet|annet";
//    private static final String LASTET_OPP = "LastetOpp";
//
//    private VedleggMapper() {
//    }
//
//    public static VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi, List<OpplastetVedlegg> opplastedeVedlegg) {
//        List<FilFrontend> filer = mapJsonFilerAndOpplastedeVedleggToFilerFrontend(vedlegg.getFiler(), opplastedeVedlegg);
//
//        String vedleggType = getSammensattNavn(vedlegg);
//
//        List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedleggType);
//
//        return new VedleggFrontend().withType(vedleggType)
//                .withGruppe(getGruppe(vedleggType))
//                .withRader(rader)
//                .withVedleggStatus(vedlegg.getStatus())
//                .withFiler(filer);
//    }
//
//    private static List<VedleggRadFrontend> getRader(JsonOkonomi jsonOkonomi, String vedleggType) {
//        if (!isInSoknadJson(vedleggType)) return Collections.emptyList();
//
//        String soknadType = vedleggTypeToSoknadType.get(vedleggType);
//        String soknadPath = getSoknadPath(vedleggType);
//
//        // Spesialtilfelle for avdrag og renter
//        if (soknadType.equals(UTGIFTER_BOLIGLAN_AVDRAG)) {
//            return getRadListWithAvdragAndRenter(jsonOkonomi);
//        }
//
//        switch (soknadPath) {
//            case "utbetaling":
//                return getRadListFromUtbetaling(jsonOkonomi, soknadType);
//            case "opplysningerUtgift":
//                return getRadListFromOpplysningerUtgift(jsonOkonomi, soknadType);
//            case "oversiktUtgift":
//                return getRadListFromOversiktUtgift(jsonOkonomi, soknadType);
//            case "formue":
//                return getRadListFromFormue(jsonOkonomi, soknadType);
//            case "inntekt":
//                return getRadListFromInntekt(jsonOkonomi, soknadType);
//        }
//
//        return null;
//    }
//
//    private static List<VedleggRadFrontend> getRadListWithAvdragAndRenter(JsonOkonomi jsonOkonomi) {
//        List<VedleggRadFrontend> avdragRad = getRadListFromOversiktUtgift(jsonOkonomi, UTGIFTER_BOLIGLAN_AVDRAG);
//        List<VedleggRadFrontend> renterRad = getRadListFromOversiktUtgift(jsonOkonomi, UTGIFTER_BOLIGLAN_RENTER);
//
//        if (avdragRad != null) {
//            for (int i = 0; i < avdragRad.size(); i++) {
//                avdragRad.get(i).withRenter(renterRad.get(i).renter);
//            }
//        }
//
//        return avdragRad;
//    }
//
//    private static List<VedleggRadFrontend> getRadListFromUtbetaling(JsonOkonomi jsonOkonomi, String soknadType) {
//        return jsonOkonomi.getOpplysninger().getUtbetaling().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
//                jsonOkonomi.getOpplysninger().getUtbetaling().stream()
//                        .filter(inntekt -> inntekt.getType().equals(soknadType))
//                        .map(VedleggMapper::getRadFromUtbetaling).collect(Collectors.toList());
//    }
//
//    private static List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String soknadType) {
//        List<VedleggRadFrontend> radList = jsonOkonomi.getOpplysninger().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
//                jsonOkonomi.getOpplysninger().getUtgift().stream()
//                        .filter(utgift -> utgift.getType().equals(soknadType))
//                        .map(utgift -> getRadFromOpplysningerUtgift(utgift, soknadType)).collect(Collectors.toList());
//        if (radList.isEmpty() && soknadType.equals(UTGIFTER_ANDRE_UTGIFTER)) {
//            return Collections.singletonList(new VedleggRadFrontend());
//        } else {
//            return radList;
//        }
//    }
//
//    private static List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String soknadType) {
//        return jsonOkonomi.getOversikt().getInntekt().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
//                jsonOkonomi.getOversikt().getInntekt().stream()
//                        .filter(inntekt -> inntekt.getType().equals(soknadType))
//                        .map(inntekt -> getRadFromInntekt(inntekt, soknadType)).collect(Collectors.toList());
//    }
//
//    private static List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String soknadType) {
//        return jsonOkonomi.getOversikt().getUtgift().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
//                jsonOkonomi.getOversikt().getUtgift().stream()
//                        .filter(utgift -> utgift.getType().equals(soknadType))
//                        .map(utgift -> getRadFromOversiktUtgift(utgift, soknadType)).collect(Collectors.toList());
//    }
//
//    private static List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String soknadType) {
//        return jsonOkonomi.getOversikt().getFormue().isEmpty() ? Collections.singletonList(new VedleggRadFrontend()) :
//                jsonOkonomi.getOversikt().getFormue().stream()
//                        .filter(utgift -> utgift.getType().equals(soknadType))
//                        .map(formue -> new VedleggRadFrontend()
//                                .withBelop(formue.getBelop()))
//                        .collect(Collectors.toList());
//    }
//
//    private static VedleggRadFrontend getRadFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
//        if (utbetaling.getBelop() != null) {
//            return new VedleggRadFrontend().withBelop(utbetaling.getBelop());
//        } else if (utbetaling.getBrutto() != null) {
//            return new VedleggRadFrontend().withBelop(Integer.valueOf(String.valueOf(utbetaling.getBrutto())));
//        } else if (utbetaling.getNetto() != null) {
//            return new VedleggRadFrontend().withBelop(utbetaling.getNetto().intValue());
//        }
//        return new VedleggRadFrontend();
//    }
//
//    private static VedleggRadFrontend getRadFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift, String soknadType) {
//        switch (soknadType) {
//            case UTGIFTER_ANDRE_UTGIFTER:
//            case UTGIFTER_ANNET_BARN:
//            case UTGIFTER_ANNET_BO:
//            case UTGIFTER_BARN_FRITIDSAKTIVITETER:
//                return new VedleggRadFrontend().withBelop(utgift.getBelop())
//                        .withBeskrivelse(utgift.getTittel().substring(utgift.getTittel().indexOf(":") + 1) + " ");
//            default:
//                return new VedleggRadFrontend().withBelop(utgift.getBelop());
//        }
//    }
//
//    private static VedleggRadFrontend getRadFromInntekt(JsonOkonomioversiktInntekt inntekt, String soknadType) {
//        if (soknadType.equals(JOBB)) {
//            return new VedleggRadFrontend()
//                    .withBrutto(inntekt.getBrutto())
//                    .withNetto(inntekt.getNetto());
//        }
//        if (inntekt.getBrutto() != null) {
//            return new VedleggRadFrontend().withBelop(inntekt.getBrutto());
//        } else if (inntekt.getNetto() != null) {
//            return new VedleggRadFrontend().withBelop(inntekt.getNetto());
//        }
//        return new VedleggRadFrontend();
//    }
//
//    private static VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String soknadType) {
//        if (soknadType.equals(UTGIFTER_BOLIGLAN_AVDRAG)) {
//            return new VedleggRadFrontend().withAvdrag(utgift.getBelop());
//        } else if (soknadType.equals(UTGIFTER_BOLIGLAN_RENTER)) {
//            return new VedleggRadFrontend().withRenter(utgift.getBelop());
//        }
//        return new VedleggRadFrontend().withBelop(utgift.getBelop());
//    }
//
//    public static List<FilFrontend> mapJsonFilerAndOpplastedeVedleggToFilerFrontend(List<JsonFiler> filer, List<OpplastetVedlegg> opplastedeVedlegg) {
//        return filer.stream().map(fil -> {
//            OpplastetVedlegg opplastetVedlegg = opplastedeVedlegg.stream().filter(oVedlegg -> oVedlegg.getFilnavn().equals(fil.getFilnavn()))
//                    .findFirst()
//                    .orElseThrow(() -> new IllegalStateException("Vedlegget finnes ikke"));
//            return new FilFrontend().withFilNavn(fil.getFilnavn()).withUuid(opplastetVedlegg.getUuid());
//        }).collect(Collectors.toList());
//    }
//
//    public static List<EttersendtVedlegg> mapVedleggToSortedListOfEttersendteVedlegg(LocalDateTime innsendingstidspunkt, List<OpplastetVedlegg> opplastedeVedlegg, List<JsonVedlegg> originaleVedlegg) {
//        SortedMap<String, EttersendtVedlegg> ettersendteVedlegg = new TreeMap<>(sortAlphabeticallyAndPutTypeAnnetLast());
//
//        originaleVedlegg.stream()
//                .filter(vedlegg -> filterGittInnsendingstidspunkt(innsendingstidspunkt, vedlegg))
//                .forEach(vedlegg -> {
//                    var sammensattNavn = getSammensattNavn(vedlegg);
//                    if (!ettersendteVedlegg.containsKey(sammensattNavn)) {
//                        List<FilFrontend> filerFrontend = new ArrayList<>();
//                        if (vedlegg.getStatus().equals(LASTET_OPP)) {
//                            List<JsonFiler> filer = vedlegg.getFiler();
//                            filerFrontend = mapJsonFilerAndOpplastedeVedleggToFilerFrontend(filer, opplastedeVedlegg);
//                        }
//
//                        ettersendteVedlegg.put(sammensattNavn, new EttersendtVedlegg()
//                                .withType(sammensattNavn)
//                                .withVedleggStatus(vedlegg.getStatus())
//                                .withFiler(filerFrontend));
//                    }
//                });
//
//        return new ArrayList<>(ettersendteVedlegg.values());
//    }
//
//    private static boolean filterGittInnsendingstidspunkt(LocalDateTime innsendingstidspunkt, JsonVedlegg vedlegg) {
//        if (innsendingstidspunkt != null && soknadSendtForMindreEnn30DagerSiden(innsendingstidspunkt.toLocalDate())) {
//            return true;
//        }
//        return vedlegg.getStatus().equals(LASTET_OPP) || getSammensattNavn(vedlegg).equals(ANNET_ANNET);
//    }
//
//    private static String getSammensattNavn(JsonVedlegg vedlegg) {
//        return vedlegg.getType() + "|" + vedlegg.getTilleggsinfo();
//    }
//
//    private static Comparator<String> sortAlphabeticallyAndPutTypeAnnetLast() {
//        return (o1, o2) -> {
//            if (o1.equals(o2)) {
//                return 0;
//            } else if (o1.equals(ANNET_ANNET)) {
//                return 1;
//            } else if (o2.equals(ANNET_ANNET)) {
//                return -1;
//            }
//            return o1.compareTo(o2);
//        };
//    }
//}
