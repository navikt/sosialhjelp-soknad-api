package no.nav.sbl.sosialhjelp.sendtsoknad;

import no.nav.sbl.sosialhjelp.domain.SendtSoknad;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SendtSoknadRepository {

    Long opprettSendtSoknad(SendtSoknad sendtSoknad, String eier);
    Optional<SendtSoknad> hentSendtSoknad(String behandlingsId, String eier);
    List<SendtSoknad> hentAlleSendteSoknader(String eier);
    void oppdaterSendtSoknadVedSendingTilFiks(String fiksforsendelseId, String behandlingsId, String eier);

    List<SendtSoknad> hentBehandlingskjede(String behandlingsId);

    List<SendtSoknad> hentSoknaderForEttersending(String fnr, LocalDateTime tidsgrense);

    void slettSendtSoknad(SendtSoknad sendtSoknad, String eier);
}
