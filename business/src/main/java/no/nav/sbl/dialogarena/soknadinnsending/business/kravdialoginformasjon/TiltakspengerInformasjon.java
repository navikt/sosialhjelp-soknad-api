package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TiltakspengerInformasjon extends KravdialogInformasjon.KravdialogInformasjonUtenAlternativRepresentasjon {

    public String getSoknadTypePrefix() {
        return "tiltakspenger";
    }

    public String getSoknadUrlKey() {
        return "tiltakspenger.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "tiltakspenger.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.singletonList(BOLK_PERSONALIA);
    }

    public String getStrukturFilnavn() {
        return "tiltakspenger.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("SKJEMANUMMER");
    }
}
