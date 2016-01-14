package no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg;


import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;

import java.util.List;

public interface VedleggRepository {

    Long opprettEllerEndreVedlegg(final Vedlegg vedlegg, byte[] content);

    void slettVedlegg(Long soknadId, Long vedleggId);

    List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse);

    void lagreVedleggMedData(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    byte[] hentVedleggData(Long vedleggId);

    Vedlegg hentVedlegg(Long vedleggId);

    Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer);

    Vedlegg hentVedleggForskjemaNummerMedTillegg(Long soknadId, Long faktumId, String skjemaNummer, String skjemanummerTillegg);

    void slettVedleggUnderBehandling(Long soknadId, Long faktumId, String gosysId, String skjemanummerTillegg);

    Vedlegg hentVedleggMedInnhold(Long vedleggId);

    void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    List<Vedlegg> hentPaakrevdeVedlegg(Long faktumId);

    /**
     * Henter alle vedlegg for en behandlingsid uavnehgig av status på vedlegget.
     * @param behandlingsId behandlingsid
     * @return liste med alle vedlegg til behandlingenuavhengig av status
     */
    List<Vedlegg> hentVedlegg(String behandlingsId);

    List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktumId);

    void slettVedleggOgData(Long soknadId, Vedlegg vedlegg);

    void slettVedleggMedVedleggId(Long vedleggId);

    String hentBehandlingsIdTilVedlegg(Long vedleggId);

    /**
     * Lagrer eller oppdaterer et vedlegg, uten å endre opplastet innhold.
     * @param vedlegg Nytt eller endret vedlegg.
     */
    void opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(Vedlegg vedlegg);
}
