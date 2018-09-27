package no.nav.sbl.dialogarena.sosialhjelp.vedlegg;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;

import java.util.List;

public interface OpplastetVedleggRepository {

    Vedlegg hentVedlegg(String uuid, String eier); //trengs denne?
    List<Vedlegg> hentVedleggForSoknad(int soknadId, String eier);
    String opprettVedlegg(String eier, String type, byte[] data);
    void slettVedlegg(String uuid, String eier);
    void slettAlleVedleggForSoknad(int soknadId, String eier);

}
