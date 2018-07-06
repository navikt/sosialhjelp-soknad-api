package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public class StaticMetoder {

    public static String skjemanummer(WebSoknad soknad) {
        return soknad.getskjemaNummer();
    }

    public static String journalforendeEnhet(WebSoknad soknad) {
        return soknad.getJournalforendeEnhet();
    }

}
