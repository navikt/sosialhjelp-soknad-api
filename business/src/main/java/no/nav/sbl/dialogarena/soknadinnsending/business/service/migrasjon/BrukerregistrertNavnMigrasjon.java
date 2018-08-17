package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class BrukerregistrertNavnMigrasjon {

    public WebSoknad migrer(WebSoknad soknad) {
        migrerNavnForBrukerregistrertEktefelle(soknad);
        migrerNavnForBrukerregistrertBarn(soknad);

        return soknad;
    }

    private void migrerNavnForBrukerregistrertEktefelle(WebSoknad soknad) {
        Faktum brukerregistrertEktefelleFaktum = soknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle");
        migrerNavnForFaktum(brukerregistrertEktefelleFaktum);
    }

    private void migrerNavnForBrukerregistrertBarn(WebSoknad soknad) {
        List<Faktum> brukerregistrertBarnFaktumListe = soknad.getFaktaMedKey("familie.barn.true.barn");
        if (brukerregistrertBarnFaktumListe != null && !brukerregistrertBarnFaktumListe.isEmpty()) {
            for (Faktum brukerregistrertBarnFaktum : brukerregistrertBarnFaktumListe) {
                migrerNavnForFaktum(brukerregistrertBarnFaktum);
            }
        }
    }

    private void migrerNavnForFaktum(Faktum faktum) {
        if (faktum != null) {
            Map<String, String> faktumProperties = faktum.getProperties();
            if (faktumProperties != null && !isEmpty(faktumProperties.get("navn")) && isEmpty(faktumProperties.get("fornavn"))) {
                String navn = faktumProperties.get("navn");
                faktumProperties.put("fornavn", fornavnFraNavn(navn));
                faktumProperties.put("mellomnavn", "");
                faktumProperties.put("etternavn", etternavnFraNavn(navn));
            }
        }
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
