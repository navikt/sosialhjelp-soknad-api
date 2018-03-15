package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.RUTES_I_BRUT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.getSkjemanummer;

public class StaticMetoder {

    public static String skjemanummer(WebSoknad soknad) {
        return soknad.erDagpengeSoknad() ? getSkjemanummer(soknad) : soknad.getskjemaNummer();
    }

    public static String journalforendeEnhet(WebSoknad soknad) {
        String journalforendeEnhet;

        if (soknad.erDagpengeSoknad()) {
            journalforendeEnhet = RUTES_I_BRUT;
        } else {
            journalforendeEnhet = soknad.getJournalforendeEnhet();
        }
        return journalforendeEnhet;
    }

}
