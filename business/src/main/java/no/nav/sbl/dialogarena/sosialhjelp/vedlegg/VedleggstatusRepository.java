package no.nav.sbl.dialogarena.sosialhjelp.vedlegg;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;

import java.util.List;

public interface VedleggstatusRepository {

    Vedlegg hentVedlegg(int id, String eier); //trengs denne?
    List<Vedlegg> hentVedleggForSendtSoknad(int sendtSoknadId, String eier);
    List<Vedlegg> hentVedleggForSendtSoknadMedStatus(int sendtSoknadId, String eier, String status);
    int opprettVedlegg(String eier, String type, byte[] data);
    void endreStatusForVedlegg(int id, String eier, String status);
    void slettVedlegg(int id, String eier);
}
