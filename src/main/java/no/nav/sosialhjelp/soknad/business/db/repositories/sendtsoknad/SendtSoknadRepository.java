package no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad;

import no.nav.sosialhjelp.soknad.domain.SendtSoknad;

import java.util.Optional;

public interface SendtSoknadRepository {

    Long opprettSendtSoknad(SendtSoknad sendtSoknad, String eier);
    Optional<SendtSoknad> hentSendtSoknad(String behandlingsId, String eier);
    void oppdaterSendtSoknadVedSendingTilFiks(String fiksforsendelseId, String behandlingsId, String eier);
}
