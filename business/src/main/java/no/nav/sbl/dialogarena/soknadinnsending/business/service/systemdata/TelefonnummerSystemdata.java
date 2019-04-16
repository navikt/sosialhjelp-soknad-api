package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class TelefonnummerSystemdata implements Systemdata {

    @Inject
    private PersonaliaFletter personaliaFletter;


    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        JsonTelefonnummer telefonnummer = personalia.getTelefonnummer();
        String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        if (telefonnummer.getKilde() == JsonKilde.SYSTEM) {
            telefonnummer.setVerdi(innhentSystemverdiTelefonnummer(personIdentifikator));
        }
    }

    public String innhentSystemverdiTelefonnummer(String personIdentifikator) {
        Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        String systemVerdi = norskTelefonnummer(personalia.getMobiltelefonnummer());
        return systemVerdi;
    }

    static String norskTelefonnummer(String mobiltelefonnummer) {
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