package no.nav.sosialhjelp.soknad.business.soknadunderbehandling;


import java.util.List;

public interface BatchSoknadUnderArbeidRepository {

    List<Long> hentGamleSoknadUnderArbeidForBatch();
    void slettSoknad(Long soknadUnderArbeidId);

}
