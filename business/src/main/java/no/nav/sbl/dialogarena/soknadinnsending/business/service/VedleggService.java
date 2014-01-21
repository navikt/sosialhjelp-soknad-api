package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.io.InputStream;
import java.util.List;

/**
 * Metoder som manippulerer vedlegg.
 */
public interface VedleggService {

    List<Long> splitOgLagreVedlegg(Vedlegg vedlegg, InputStream inputStream);

    List<Vedlegg> hentVedleggUnderBehandling(Long soknadId, Long faktumId, String gosysId);

    Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold);

    void slettVedlegg(Long soknadId, Long vedleggId);

    byte[] lagForhandsvisning(Long soknadId, Long vedleggId, int side);

    Long genererVedleggFaktum(Long soknadId, Long vedleggId);

    List<Vedlegg> hentPaakrevdeVedlegg(Long soknadId);

    void lagreVedlegg(Long soknadId, Long vedleggId,  Vedlegg vedlegg);
}
