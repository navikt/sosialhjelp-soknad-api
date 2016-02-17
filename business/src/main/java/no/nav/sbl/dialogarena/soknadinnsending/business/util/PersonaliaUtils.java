package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.PersonaliaBuilder;

import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;

public class PersonaliaUtils {

    public static Personalia adresserOgStatsborgerskap(WebSoknad webSoknad) {
        Map<String, String> properties = webSoknad.getFaktaMedKey(PERSONALIA_KEY).get(0).getProperties();

        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdresse(properties.get(GJELDENDEADRESSE_KEY));
        gjeldendeAdresse.setAdressetype(properties.get(GJELDENDEADRESSE_TYPE_KEY));
        gjeldendeAdresse.setLandkode(properties.get(GJELDENDEADRESSE_LANDKODE));
        Adresse senkundarAdresse = new Adresse();
        senkundarAdresse.setAdresse(properties.get(SEKUNDARADRESSE_KEY));
        senkundarAdresse.setAdressetype(properties.get(SEKUNDARADRESSE_TYPE_KEY));
        senkundarAdresse.setLandkode(properties.get(SEKUNDARADRESSE_LANDKODE));
        return PersonaliaBuilder.with()
                .gjeldendeAdresse(gjeldendeAdresse).sekundarAdresse(senkundarAdresse).statsborgerskap(properties.get(STATSBORGERSKAP_KEY))
                .build();
    }
}
