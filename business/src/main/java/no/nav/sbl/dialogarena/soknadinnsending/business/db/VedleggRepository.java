package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.io.InputStream;
import java.util.List;

/**
 * Operasjoner for å lagre og manipulere vedlegg i basen.
 */
public interface VedleggRepository {

    /**
     * Lagrer et nytt vedlegg
     *
     * @param vedlegg vedlegget som skal lagres
     * @param content innholdet i vedlegget
     * @return
     */
    Long opprettVedlegg(final Vedlegg vedlegg, byte[] content);

    /**
     * Sletter et bestemt vedlegg
     *
     * @param soknadId  soknaden det skal slettes for
     * @param vedleggId vedlegget som skal slettes
     */
    void slettVedlegg(Long soknadId, Long vedleggId);

    /**
     * Henter alle vedlegg for et faktum
     *
     * @param soknadId soknaden det skal hentes for
     * @param faktum   faktument det skal hentes for
     * @param skjemaNummer  hvilket vedlegg det skal hentes for
     * @return en liste med vedlegg
     */
    List<Vedlegg> hentVedleggUnderBehandling(Long soknadId, Long faktum, String skjemaNummer);

    void lagreVedleggMedData(Long soknadId, Long vedleggId, Vedlegg vedlegg);

    /**
     * Henter innholdet til ett bestemt vedlegg
     *
     * @param soknadId  soknaden det skal hentes for
     * @param vedleggId vedlegget som skal hentes
     * @return innholdet
     */
    InputStream hentVedleggStream(Long soknadId, Long vedleggId);

    /**
     * Henter et spesifikt vedlegg uten data
     *
     * @param soknadId  soknaden det skal hentes for
     * @param vedleggId vedlegget som skal hentes
     * @return vedlegget
     */
    Vedlegg hentVedlegg(Long soknadId, Long vedleggId);

    /**
     * Henter vedlegg for et faktum og skjemaNummer
     *
     * @param soknadId soknaden det skal hentes for
     * @param faktumId faktumet det skal hentes vedlegg for
     * @param skjemaNummer  skjemaNummer til faktumet
     * @return vedlegget
     */
    Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer);


    /**
     * Sletter alle vedlegg for et bestemt faktum
     *
     * @param soknadId soknaden det jobbes på
     * @param faktumId faktumet det skal slettes vedlegg for
     * @param gosysId
     */
    void slettVedleggUnderBehandling(Long soknadId, Long faktumId, String gosysId);

    /**
     * Henter ett vedleg med innhold
     *
     * @param soknadId  soknaden det skal hentes for
     * @param vedleggId vedlegget som skal hentes
     * @return vedlegget med innhold
     */
    Vedlegg hentVedleggMedInnhold(Long soknadId, Long vedleggId);

    void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg);
}
