package no.nav.sbl.dialogarena.sosialhjelp.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public interface SoknadUnderArbeidRepository {

    int opprettSoknad(WebSoknad webSoknad);
    WebSoknad hentSoknad(int id, String eier);
    WebSoknad hentSoknad(String behandlingsId, String eier);
    void oppdatereSoknadsdata(WebSoknad webSoknad, String eier);
    void slettSoknad(WebSoknad webSoknad, String eier);
}
