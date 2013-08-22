package no.nav.sbl.dialogarena.dokumentinnsending.service;


import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;

public interface SoknadService {

    Long leggTilVedlegg(String behandlingsId, String vedleggsTekst);

    DokumentSoknad hentSoknad(String behandlingsId);

    Dokument hentDokument(long dokumentId, String behandlingsId);

    Dokument hentOppdatertDokument(Dokument dokument);

    void oppdaterInnsendingsvalg(Dokument dokument);

    /**
     * Legger til innhold til et dokument
     *
     * @param dokument dokumentet som skal opptateres
     * @param innhold  Innholdet til dokumentet
     */
    void oppdaterInnhold(Dokument dokument, DokumentInnhold innhold);

    /**
     * Sletter innholdet til et dokument
     *
     * @param dokument dokumentet det skal slettes fra
     */
    void slettInnhold(Dokument dokument);

    void oppdaterBeskrivelseAnnetVedlegg(Dokument dokument, String beskrivelse);

    void slettSoknad(String behandlingsId);

    /**
     * Henter innhold til dokumentet.
     *
     * @param dokument dokumentet innholdet skal hentes fra
     * @return innholdet til dokumentet
     */
    DokumentInnhold hentDokumentInnhold(Dokument dokument);

    /**
     * Henter dokument med gitt id.
     *
     * @param dokumentId id til dokumentet
     * @return innholdet
     */
    DokumentInnhold hentDokumentInnhold(Long dokumentId);
}