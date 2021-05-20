package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;


import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;

import java.util.List;
import java.util.Optional;

public interface BatchSoknadUnderArbeidRepository {

    Optional<Long> hentSoknadUnderArbeidIdFromBehandlingsIdOptional(String behandlingsId);
    List<Long> hentGamleSoknadUnderArbeidForBatch();
    void slettSoknad(Long soknadUnderArbeidId);
    List<SoknadUnderArbeid> hentForeldedeEttersendelser();

}
