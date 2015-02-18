package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.io.InputStream;
import java.util.List;

public interface VedleggService {

    List<Long> splitOgLagreVedlegg(Vedlegg vedlegg, InputStream inputStream);

    List<Vedlegg> hentVedleggUnderBehandling(Long soknadId, String fillagerReferanse);

    Vedlegg hentVedlegg(String behandlingsId, Long vedleggId, boolean medInnhold);
    Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold);

    void slettVedlegg(Long soknadId, Long vedleggId);

    byte[] lagForhandsvisning(Long soknadId, Long vedleggId, int side);

    Long genererVedleggFaktum(Long soknadId, Long vedleggId);
    Long genererVedleggFaktum(String behandlingsId, Long vedleggId);

    List<Vedlegg> hentPaakrevdeVedlegg(Long faktumId);
    List<Vedlegg> hentPaakrevdeVedlegg(String behandlingsId);

    void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    void leggTilKodeverkFelter(List<Vedlegg> vedlegg);

    void lagreKvitteringSomVedlegg(String behandlingsId, byte[] kvittering);

}
