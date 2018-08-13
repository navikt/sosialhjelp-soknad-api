package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class BrukerregistrertEktefelleNavnMigrasjon extends Migrasjon {

    public BrukerregistrertEktefelleNavnMigrasjon() {
        super(2);
    }

    @Override
    public WebSoknad migrer(Integer fraVersjon, WebSoknad soknad) {
        soknad.medVersjon(tilVersjon);

        Faktum brukerregistrertEktefelleFaktum = soknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle");
        if (brukerregistrertEktefelleFaktum == null) {
            return soknad;
        }

        Map<String, String> brukerregistrertEktefelleProperties = brukerregistrertEktefelleFaktum.getProperties();
        if (brukerregistrertEktefelleProperties != null && !isEmpty(brukerregistrertEktefelleProperties.get("navn"))) {
            String navn = brukerregistrertEktefelleProperties.get("navn");
            brukerregistrertEktefelleProperties.put("fornavn", fornavnFraNavn(navn));
            brukerregistrertEktefelleProperties.put("mellomnavn", "");
            brukerregistrertEktefelleProperties.put("etternavn", etternavnFraNavn(navn));
        }
        return soknad;
    }

    private static String fornavnFraNavn(String navn) {
        if (navn == null) {
            return "";
        }

        final String trimmedNavn = navn.trim();
        if (!trimmedNavn.contains(" ")) {
            return navn;
        }
        return trimmedNavn.substring(0, trimmedNavn.lastIndexOf(' '));
    }

    private static String etternavnFraNavn(String navn) {
        if (navn == null || !navn.trim().contains(" ")) {
            return "";
        }

        final String trimmedNavn = navn.trim();
        return trimmedNavn.substring(trimmedNavn.lastIndexOf(' ') + 1);
    }
}
