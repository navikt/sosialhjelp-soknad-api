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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addUtgiftIfNotPresentInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.removeUtgiftIfPresentInOpplysninger;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.*;
import static org.slf4j.LoggerFactory.getLogger;

public class OkonomiskeOpplysningerMapper {
    private static final Logger logger = getLogger(OkonomiskeOpplysningerMapper.class);

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
        } else {
            logger.error("Typen \'" + soknadType + "\' eksisterer ikke fra før av i søknad.json");
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
        } else {
            logger.error("Typen \'" + soknadType + "\' eksisterer ikke fra før av i søknad.json");
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
            if (soknadType.equals(UTGIFTER_BOLIGLAN_AVDRAG)){
                addBoliglanRenterToUtgifter(vedleggFrontend, jsonOkonomi, utgifter);
            }
            // ----------------------------------------------------------------------------------------

            jsonOkonomi.getOversikt().setUtgift(utgifter);
        } else {
            logger.error("Typen \'" + soknadType + "\' eksisterer ikke fra før av i søknad.json");
        }
    }

    public static void addAllOpplysningUtgifterToJsonOkonomi(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, String soknadType) {
        Optional<JsonOkonomiOpplysningUtgift> eksisterendeOpplysningUtgift = jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .findFirst();

        if (vedleggFrontend.type.equals("annet|annet")){
            eksisterendeOpplysningUtgift = Optional.of(new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_ANDRE_UTGIFTER).withTittel("Annen (brukerangitt): "));
            final List<JsonOkonomiOpplysningUtgift> utgifter = jsonOkonomi.getOpplysninger().getUtgift();
            if (checkIfTypeAnnetAnnetShouldBeRemoved(vedleggFrontend)){
                removeUtgiftIfPresentInOpplysninger(utgifter, soknadType);
                return;
            } else {
                addUtgiftIfNotPresentInOpplysninger(utgifter, soknadType, eksisterendeOpplysningUtgift.get().getTittel());
            }
        }

        if (eksisterendeOpplysningUtgift.isPresent()) {
            final List<JsonOkonomiOpplysningUtgift> utgifter = jsonOkonomi.getOpplysninger().getUtgift().stream()
                    .filter(utgift -> !utgift.getType().equals(soknadType))
                    .collect(Collectors.toList());

            utgifter.addAll(mapToOppysningUtgiftList(vedleggFrontend.rader, eksisterendeOpplysningUtgift.get()));
            jsonOkonomi.getOpplysninger().setUtgift(utgifter);
        } else {
            logger.error("Typen: \'" + soknadType + "\' eksisterer ikke fra før av i søknad.json");
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
        } else {
            logger.error("Typen: \'" + soknadType + "\' eksisterer ikke fra før av i søknad.json");
        }
    }

    private static void addBoliglanRenterToUtgifter(VedleggFrontend vedleggFrontend, JsonOkonomi jsonOkonomi, List<JsonOkonomioversiktUtgift> utgifter) {
        String soknadType = UTGIFTER_BOLIGLAN_RENTER;
        final Optional<JsonOkonomioversiktUtgift> eksisterendeRenter = jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(soknadType))
                .findFirst();

        if (eksisterendeRenter.isPresent()) {
            utgifter.removeAll(utgifter.stream()
                    .filter(utgift -> utgift.getType().equals(soknadType))
                    .collect(Collectors.toList()));

            utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, eksisterendeRenter.get()));
        } else {
            logger.error("Typen: \'" + soknadType + "\' eksisterer ikke fra før av i søknad.json");
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
                .withBelop(type.equals(UTGIFTER_BOLIGLAN_AVDRAG) ? radFrontend.avdrag :
                        type.equals(UTGIFTER_BOLIGLAN_RENTER) ? radFrontend.renter : radFrontend.belop)
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
}
