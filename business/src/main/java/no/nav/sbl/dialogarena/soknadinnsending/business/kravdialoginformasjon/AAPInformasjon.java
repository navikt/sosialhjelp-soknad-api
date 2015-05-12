package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.List;

import static java.util.Arrays.asList;


public class AAPInformasjon implements KravdialogInformasjon {

    private static List<String> skjemanummer = asList("NAV 11-13.05");

    public String getSoknadTypePrefix () {
        return "aap.ordinaer";
    }

    public String getSoknadUrlKey() {
        return "soknad.aap.ordinaer.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "aap_ordinaer.xml";
    }

    public List<String> getSkjemanummer() {
        return skjemanummer;
    }

    public List<String> getSoknadBolker() {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public static boolean erAap(String skjema) {
        return skjemanummer.contains(skjema);
    }
}
