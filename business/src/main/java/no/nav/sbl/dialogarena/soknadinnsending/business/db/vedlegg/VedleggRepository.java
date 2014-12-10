package no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.util.List;

public interface VedleggRepository {

    Long opprettVedlegg(final Vedlegg vedlegg, byte[] content);

    void slettVedlegg(Long soknadId, Long vedleggId);

    List<Vedlegg> hentVedleggUnderBehandling(Long soknadId, String fillagerReferanse);

    void lagreVedleggMedData(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    byte[] hentVedleggData(Long soknadId, Long vedleggId);

    Vedlegg hentVedlegg(Long soknadId, Long vedleggId);

    Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer);

    void slettVedleggUnderBehandling(Long soknadId, Long faktumId, String gosysId);

    Vedlegg hentVedleggMedInnhold(Long soknadId, Long vedleggId);

    void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    List<Vedlegg> hentPaakrevdeVedlegg(Long soknadId);

    List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktumId);

    void slettVedleggOgData(Long soknadId, Long faktumId, String skjemaNummer);

    void slettVedleggMedVedleggId(Long vedleggId);

}
