package no.nav.sbl.sosialhjelp.vedlegg;

import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;

import java.util.List;

public interface VedleggstatusRepository {

    Vedleggstatus hentVedlegg(Long vedleggstatusId, String eier);
    List<Vedleggstatus> hentVedleggForSendtSoknad(Long sendtSoknadId, String eier);
    List<Vedleggstatus> hentVedleggForSendtSoknadMedStatus(Long sendtSoknadId, String status, String eier);
    Long opprettVedlegg(Vedleggstatus vedleggstatus, String eier);
    void endreStatusForVedlegg(Long vedleggstatusId, String status, String eier);
    void slettVedlegg(Long vedleggstatusId, String eier);
}
