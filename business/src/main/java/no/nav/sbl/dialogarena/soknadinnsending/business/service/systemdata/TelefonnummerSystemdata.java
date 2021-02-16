package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class TelefonnummerSystemdata implements Systemdata {

    @Inject
    private DkifService dkifService;

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
        String mobiltelefonnummer = dkifService.hentMobiltelefonnummer(personIdentifikator);
        return norskTelefonnummer(mobiltelefonnummer);
    }

    private static String norskTelefonnummer(String mobiltelefonnummer) {
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