package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService;
import org.springframework.stereotype.Component;


@Component
public class KontonummerSystemdata implements Systemdata {

    private final KontonummerService kontonummerService;

    public KontonummerSystemdata(
            KontonummerService kontonummerService
    ) {
        this.kontonummerService = kontonummerService;
    }

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final JsonKontonummer kontonummer = personalia.getKontonummer();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        if (kontonummer.getKilde() == JsonKilde.SYSTEM) {
            String systemverdi = innhentSystemverdiKontonummer(personIdentifikator);
            if (systemverdi == null || systemverdi.isEmpty()) {
                kontonummer.setKilde(JsonKilde.BRUKER);
                kontonummer.setVerdi(null);
            } else {
                String verdi = systemverdi.replaceAll("\\D", "");
                kontonummer.setVerdi(verdi);
            }
        }
    }

    public String innhentSystemverdiKontonummer(final String personIdentifikator) {
        return kontonummerService.getKontonummer(personIdentifikator);
    }
}
