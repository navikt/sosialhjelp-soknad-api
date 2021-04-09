package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.finn.unleash.Unleash;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.consumer.personv3.PersonServiceV3;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.Kontonummer;
import no.nav.sosialhjelp.soknad.oppslag.KontonummerService;
import org.springframework.stereotype.Component;


@Component
public class KontonummerSystemdata implements Systemdata {

    private static final String FEATURE_OPPSLAG_KONTONUMMER_ENABLED = "sosialhjelp.oppslag.kontonummer.enabled";

    private final PersonServiceV3 personService;
    private final KontonummerService kontonummerService;
    private final Unleash unleash;

    public KontonummerSystemdata(
            PersonServiceV3 personService,
            KontonummerService kontonummerService,
            Unleash unleash
    ) {
        this.personService = personService;
        this.kontonummerService = kontonummerService;
        this.unleash = unleash;
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
        if (unleash.isEnabled(FEATURE_OPPSLAG_KONTONUMMER_ENABLED, false)) {
            return kontonummerService.getKontonummer(personIdentifikator);
        }
        Kontonummer kontonummer = personService.hentKontonummer(personIdentifikator);
        if (kontonummer == null) {
            return null;
        }
        return kontonummer.getKontonummer();

    }
}
