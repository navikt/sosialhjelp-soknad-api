package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.client.dkif.MobiltelefonService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class TelefonnummerSystemdata implements Systemdata {

    private static final Logger log = getLogger(TelefonnummerSystemdata.class);

    private final MobiltelefonService mobiltelefonService;

    public TelefonnummerSystemdata(
            MobiltelefonService mobiltelefonService
    ) {
        this.mobiltelefonService = mobiltelefonService;
    }

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final JsonTelefonnummer telefonnummer = personalia.getTelefonnummer();

        if (telefonnummer == null || telefonnummer.getKilde() == JsonKilde.SYSTEM) {
            final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
            final String systemverdi = innhentSystemverdiTelefonnummer(personIdentifikator);

            personalia.setTelefonnummer(systemverdi == null ? null :
                    telefonnummer != null ? telefonnummer.withVerdi(systemverdi) :
                            new JsonTelefonnummer()
                                    .withKilde(JsonKilde.SYSTEM)
                                    .withVerdi(systemverdi));
        }
    }

    public String innhentSystemverdiTelefonnummer(final String personIdentifikator) {
        try {
            return norskTelefonnummer(mobiltelefonService.hent(personIdentifikator));
        } catch (Exception e) {
            log.warn("Kunne ikke hente telefonnummer fra Dkif (ny client). Prøver gammel client", e);
            return null;
        }
    }

    private String norskTelefonnummer(String mobiltelefonnummer) {
        if (mobiltelefonnummer == null) {
            return null;
        }
        if (mobiltelefonnummer.length() == 8) {
            return "+47" + mobiltelefonnummer;
        }
        if (mobiltelefonnummer.startsWith("+47") && mobiltelefonnummer.length() == 11) {
            return mobiltelefonnummer;
        }
        return null;
    }
}