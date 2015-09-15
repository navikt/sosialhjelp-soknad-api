package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.List;

import static java.util.Arrays.asList;

public class AAPGjenopptakInformasjon extends KravdialogInformasjon.DefaultOppsett {

    private static List<String> skjemanummer = asList("NAV 11-13.06");

    public String getSoknadTypePrefix() {
        return "aap.gjenopptak";
    }

    public String getSoknadUrlKey() {
        return "soknad.aap.gjenopptak.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.fortsett.path";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public String getStrukturFilnavn() {
        return "aap_ordinaer.xml";
    }

    @Override
    public List<String> getSkjemanummer() {
        return skjemanummer;
    }

    public static boolean erAapGjenopptak(String skjema) {
        return skjemanummer.contains(skjema);
    }
}
