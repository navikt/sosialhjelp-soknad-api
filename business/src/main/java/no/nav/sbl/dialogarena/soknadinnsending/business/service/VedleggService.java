package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.io.InputStream;
import java.util.List;

public interface VedleggService {

    List<Long> splitOgLagreVedlegg(Vedlegg vedlegg, InputStream inputStream);

    List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse);

    Vedlegg hentVedlegg(Long vedleggId);
    Vedlegg hentVedlegg(Long vedleggId, boolean medInnhold);

    String hentBehandlingsId(Long vedleggId);

    void slettVedlegg(Long vedleggId);

    byte[] lagForhandsvisning(Long vedleggId, int side);

    Long genererVedleggFaktum(String behandlingsId, Long vedleggId);

    List<Vedlegg> hentPaakrevdeVedlegg(Long faktumId);
    List<Vedlegg> hentPaakrevdeVedlegg(String behandlingsId);

    List<Vedlegg> hentPaakrevdeVedleggMedGenerering(String behandlingsId);

    void lagreVedlegg(Long vedleggId, Vedlegg vedlegg);

    void leggTilKodeverkFelter(List<Vedlegg> vedlegg);

    void lagreKvitteringSomVedlegg(String behandlingsId, byte[] kvittering);

}
