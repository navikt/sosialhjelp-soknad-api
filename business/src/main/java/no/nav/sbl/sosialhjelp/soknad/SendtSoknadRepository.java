package no.nav.sbl.sosialhjelp.soknad;

import no.nav.sbl.sosialhjelp.domain.SendtSoknad;

public interface SendtSoknadRepository {

    Long opprettSendtSoknad(SendtSoknad sendtSoknad, String eier);
    SendtSoknad hentSendtSoknad(String behandlingsId, String eier);
    void slettSendtSoknad(SendtSoknad sendtSoknad, String eier);
}
