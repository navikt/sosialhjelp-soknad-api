package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.io.InputStream;
import java.util.List;

/**
 * Metoder som manippulerer vedlegg.
 */
public interface VedleggService {
    Long lagreVedlegg(Vedlegg vedlegg, InputStream inputStream);

    List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktumId);

    Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold);

    void slettVedlegg(Long soknadId, Long vedleggId);

    byte[] lagForhandsvisning(Long soknadId, Long vedleggId);

    Long genererVedleggFaktum(Long soknadId, Long faktumId);
}
