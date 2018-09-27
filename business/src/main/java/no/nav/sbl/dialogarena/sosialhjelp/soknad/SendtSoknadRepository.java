package no.nav.sbl.dialogarena.sosialhjelp.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public interface SendtSoknadRepository {

    int opprettSendtSoknad(WebSoknad webSoknad);
    WebSoknad hentSendtSoknad(String behandlingsId, String eier);
    void slettSendtSoknad(WebSoknad webSoknad, String eier);
}
