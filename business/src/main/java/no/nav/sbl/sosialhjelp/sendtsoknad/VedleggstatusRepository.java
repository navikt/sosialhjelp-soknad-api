package no.nav.sbl.sosialhjelp.sendtsoknad;

import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;

import java.util.List;
import java.util.Optional;

public interface VedleggstatusRepository {

    Optional<Vedleggstatus> hentVedlegg(Long vedleggstatusId, String eier);
    List<Vedleggstatus> hentVedleggForSendtSoknad(Long sendtSoknadId, String eier);
    List<Vedleggstatus> hentVedleggForSendtSoknadMedStatus(Long sendtSoknadId, String status, String eier);
    Long opprettVedlegg(Vedleggstatus vedleggstatus, String eier);
    void endreStatusForVedlegg(Long vedleggstatusId, String status, String eier);
    void slettVedlegg(Long vedleggstatusId, String eier);
    void slettAlleVedleggForSendtSoknad(Long sendtSoknadId, String eier);
}
