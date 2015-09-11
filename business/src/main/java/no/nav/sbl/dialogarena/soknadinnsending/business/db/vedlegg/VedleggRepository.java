package no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.util.List;

public interface VedleggRepository {

    Long opprettVedlegg(final Vedlegg vedlegg, byte[] content);

    void slettVedlegg(Long soknadId, Long vedleggId);

    List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse);

    void lagreVedleggMedData(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    byte[] hentVedleggData(Long vedleggId);

    Vedlegg hentVedlegg(Long vedleggId);

    Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer);

    Vedlegg hentVedleggForskjemaNummerMedTillegg(Long soknadId, Long faktumId, String skjemaNummer, String skjemanummerTillegg);

    void slettVedleggUnderBehandling(Long soknadId, Long faktumId, String gosysId);

    Vedlegg hentVedleggMedInnhold(Long vedleggId);

    void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    List<Vedlegg> hentPaakrevdeVedlegg(Long faktumId);
    List<Vedlegg> hentPaakrevdeVedlegg(String behandlingsId);

    List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktumId);

    void slettVedleggOgData(Long soknadId, Vedlegg vedlegg);

    void slettVedleggMedVedleggId(Long vedleggId);

    String hentBehandlingsIdTilVedlegg(Long vedleggId);

}
