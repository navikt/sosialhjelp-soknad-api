package no.nav.sosialhjelp.soknad.business.mappers;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;

import java.util.List;
import java.util.Optional;

public class OkonomiMapper {

    public static void setBekreftelse(JsonOkonomiopplysninger opplysninger, String type, Boolean verdi, String tittel) {
        final Optional<JsonOkonomibekreftelse> utbetaltBekreftelse = opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(type)).findFirst();

        if (utbetaltBekreftelse.isPresent()){
            utbetaltBekreftelse.get().withKilde(JsonKilde.BRUKER).withVerdi(verdi);
        } else {
            List<JsonOkonomibekreftelse> bekreftelser = opplysninger.getBekreftelse();
            bekreftelser.add(new JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withVerdi(verdi));
        }
    }

    public static void addFormueIfNotPresentInOversikt(List<JsonOkonomioversiktFormue> formuer, String type, String tittel) {
        Optional<JsonOkonomioversiktFormue> jsonFormue = formuer.stream()
                .filter(formue -> formue.getType().equals(type)).findFirst();
        if (!jsonFormue.isPresent()){
            formuer.add(new JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false));
        }
    }

    public static void addInntektIfNotPresentInOversikt(List<JsonOkonomioversiktInntekt> inntekter, String type, String tittel) {
        Optional<JsonOkonomioversiktInntekt> jsonInntekt = inntekter.stream()
                .filter(formue -> formue.getType().equals(type)).findFirst();
        if (!jsonInntekt.isPresent()){
            inntekter.add(new JsonOkonomioversiktInntekt()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false));
        }
    }

    public static void addUtgiftIfNotPresentInOversikt(List<JsonOkonomioversiktUtgift> utgifter, String type, String tittel) {
        Optional<JsonOkonomioversiktUtgift> jsonUtgift = utgifter.stream()
                .filter(utgift -> utgift.getType().equals(type)).findFirst();
        if (!jsonUtgift.isPresent()){
            utgifter.add(new JsonOkonomioversiktUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false));
        }
    }

    public static void addUtgiftIfNotPresentInOpplysninger(List<JsonOkonomiOpplysningUtgift> utgifter, String type, String tittel) {
        Optional<JsonOkonomiOpplysningUtgift> jsonUtgift = utgifter.stream()
                .filter(utgift -> utgift.getType().equals(type)).findFirst();
        if (!jsonUtgift.isPresent()){
            utgifter.add(new JsonOkonomiOpplysningUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false));
        }
    }

    public static void addUtbetalingIfNotPresentInOpplysninger(List<JsonOkonomiOpplysningUtbetaling> utbetalinger, String type, String tittel) {
        Optional<JsonOkonomiOpplysningUtbetaling> jsonUtbetaling = utbetalinger.stream()
                .filter(utbetaling -> utbetaling.getType().equals(type)).findFirst();
        if (!jsonUtbetaling.isPresent()){
            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false));
        }
    }

    public static void removeFormueIfPresentInOversikt(List<JsonOkonomioversiktFormue> formuer, String type) {
        formuer.removeIf(formue -> formue.getType().equals(type));
    }

    public static void removeInntektIfPresentInOversikt(List<JsonOkonomioversiktInntekt> inntekter, String type) {
        inntekter.removeIf(inntekt -> inntekt.getType().equals(type));
    }

    public static void removeUtgiftIfPresentInOversikt(List<JsonOkonomioversiktUtgift> utgifter, String type) {
        utgifter.removeIf(utgift -> utgift.getType().equals(type));
    }

    public static void removeUtgiftIfPresentInOpplysninger(List<JsonOkonomiOpplysningUtgift> utgifter, String type) {
        utgifter.removeIf(utgift -> utgift.getType().equals(type));
    }

    public static void removeUtbetalingIfPresentInOpplysninger(List<JsonOkonomiOpplysningUtbetaling> utbetalinger, String type) {
        utbetalinger.removeIf(utbetaling -> utbetaling.getType().equals(type));
    }

    public static void removeBekreftelserIfPresent(JsonOkonomiopplysninger opplysninger, String type) {
        opplysninger.getBekreftelse().removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(type));
    }

    public static void addFormueIfCheckedElseDeleteInOversikt(List<JsonOkonomioversiktFormue> formuer, String type, String tittel, boolean isChecked) {
        if (isChecked){
            addFormueIfNotPresentInOversikt(formuer, type, tittel);
        } else {
            removeFormueIfPresentInOversikt(formuer, type);
        }
    }

    public static void addInntektIfCheckedElseDeleteInOversikt(List<JsonOkonomioversiktInntekt> inntekter, String type, String tittel, boolean isChecked) {
        if (isChecked){
            addInntektIfNotPresentInOversikt(inntekter, type, tittel);
        } else {
            removeInntektIfPresentInOversikt(inntekter, type);
        }
    }

    public static void addutgiftIfCheckedElseDeleteInOversikt(List<JsonOkonomioversiktUtgift> utgifter, String type, String tittel, boolean isChecked) {
        if (isChecked){
            addUtgiftIfNotPresentInOversikt(utgifter, type, tittel);
        } else {
            removeUtgiftIfPresentInOversikt(utgifter, type);
        }
    }

    public static void addutgiftIfCheckedElseDeleteInOpplysninger(List<JsonOkonomiOpplysningUtgift> utgifter, String type, String tittel, boolean isChecked) {
        if (isChecked){
            addUtgiftIfNotPresentInOpplysninger(utgifter, type, tittel);
        } else {
            removeUtgiftIfPresentInOpplysninger(utgifter, type);
        }
    }


    public static void addUtbetalingIfCheckedElseDeleteInOpplysninger(List<JsonOkonomiOpplysningUtbetaling> utbetalinger, String type, String tittel, boolean isChecked) {
        if (isChecked){
            addUtbetalingIfNotPresentInOpplysninger(utbetalinger, type, tittel);
        } else {
            removeUtbetalingIfPresentInOpplysninger(utbetalinger, type);
        }
    }
}
