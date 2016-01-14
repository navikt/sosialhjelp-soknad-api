package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;

import static java.util.Arrays.asList;


public class DagpengerOrdinaerInformasjon extends KravdialogInformasjon.DefaultOppsett {

    private static List<String> skjemanummer = asList("NAV 04-01.03", "NAV 04-01.04");

    public String getSoknadTypePrefix() {
        return "dagpenger.ordinaer";
    }

    public String getSoknadUrlKey() {
        return "soknad.dagpenger.ordinaer.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.dagpenger.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "dagpenger/dagpenger_ordinaer.xml";
    }

    public List<String> getSkjemanummer() {
        return skjemanummer;
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    @Override
    public boolean brukerNyOppsummering() {
        return true;
    }

    public static boolean erDagpengerOrdinaer(String skjema) {
        return skjemanummer.contains(skjema);
    }
}
