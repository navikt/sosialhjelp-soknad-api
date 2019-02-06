package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;

import java.util.List;
import java.util.Optional;

public class BekreftelseMapper {

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
}
