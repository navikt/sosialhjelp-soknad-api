package no.nav.sosialhjelp.soknad.business.sendtsoknad;

import no.nav.sosialhjelp.soknad.domain.SendtSoknad;

import java.util.List;
import java.util.Optional;

public interface SendtSoknadRepository {

    Long opprettSendtSoknad(SendtSoknad sendtSoknad, String eier);
    Optional<SendtSoknad> hentSendtSoknad(String behandlingsId, String eier);
    List<SendtSoknad> hentAlleSendteSoknader(String eier);
    void oppdaterSendtSoknadVedSendingTilFiks(String fiksforsendelseId, String behandlingsId, String eier);
    void slettSendtSoknad(SendtSoknad sendtSoknad, String eier);
}
