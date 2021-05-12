package no.nav.sosialhjelp.soknad.business.sendtsoknad;

import java.util.Optional;

public interface BatchSendtSoknadRepository {

    Optional<Long> hentSendtSoknad(String behandlingsId);
    void slettSendtSoknad(Long sendtSoknadId);
}
