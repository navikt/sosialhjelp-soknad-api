package no.nav.sosialhjelp.soknad.business.soknadunderbehandling;


import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;

import java.util.List;

public interface BatchSoknadUnderArbeidRepository {

    List<Long> hentGamleSoknadUnderArbeidForBatch();
    void slettSoknad(Long soknadUnderArbeidId);
    List<SoknadUnderArbeid> hentForeldedeEttersendelser();

}
