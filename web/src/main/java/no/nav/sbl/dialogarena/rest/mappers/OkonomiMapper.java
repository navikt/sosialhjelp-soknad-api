package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;

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
}
