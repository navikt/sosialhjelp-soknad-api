package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class KontonummerSystemdata implements Systemdata {
        
    @Inject
    private PersonaliaFletter personaliaFletter;

    
    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final JsonKontonummer kontonummer = personalia.getKontonummer();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        if (kontonummer.getKilde() == JsonKilde.SYSTEM) {
            String systemverdi = innhentSystemverdiKontonummer(personIdentifikator);
            if (systemverdi == null){
                kontonummer.setKilde(JsonKilde.BRUKER);
                kontonummer.setVerdi(null);
            } else {
                String verdi = systemverdi.replaceAll("\\D", "");
                kontonummer.setVerdi(verdi);
            }
        }
    }
    
    public String innhentSystemverdiKontonummer(final String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        return norskKontonummer(personalia);
    }
    
    
    private String norskKontonummer(Personalia personalia) {
        if (personalia.getErUtenlandskBankkonto() != null && personalia.getErUtenlandskBankkonto()) {
            return null;
        } else {
            final String kontonummer = personalia.getKontonummer();
            if (kontonummer == null || kontonummer.isEmpty()) {
                return null;
            }
            return kontonummer;
        }
    }
}
