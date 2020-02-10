package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.AdresserOgKontonummer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personv3.PersonServiceV3;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class KontonummerSystemdata implements Systemdata {

    @Inject
    private PersonServiceV3 personService;
    
    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final JsonKontonummer kontonummer = personalia.getKontonummer();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        if (kontonummer.getKilde() == JsonKilde.SYSTEM) {
            String systemverdi = innhentSystemverdiKontonummer(personIdentifikator);
            if (systemverdi == null || systemverdi.isEmpty()){
                kontonummer.setKilde(JsonKilde.BRUKER);
                kontonummer.setVerdi(null);
            } else {
                String verdi = systemverdi.replaceAll("\\D", "");
                kontonummer.setVerdi(verdi);
            }
        }
    }
    
    public String innhentSystemverdiKontonummer(final String personIdentifikator) {
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer(personIdentifikator);
        if (adresserOgKontonummer == null) {
            return null;
        }
        return adresserOgKontonummer.getKontonummer();
    }
}
