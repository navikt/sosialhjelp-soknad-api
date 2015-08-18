package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;


import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;

import java.util.List;

public interface SoknadRepository {

    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);
    WebSoknad hentSoknad(String behandlingsId);

    WebSoknad hentSoknadMedData(Long id);
    WebSoknad hentSoknadMedVedlegg(String behandlingsId);

    List<Faktum> hentAlleBrukerData(String behandlingsId);
    List<Faktum> hentAlleBrukerData(Long soknadId);

    Optional<WebSoknad> plukkSoknadTilMellomlagring();

    void leggTilbake(WebSoknad webSoknad);

    List<WebSoknad> hentListe(String aktorId);

    Long lagreFaktum(long soknadId, Faktum faktum);
    Long lagreFaktum(long soknadId, Faktum faktum, Boolean systemFaktum);

    Faktum hentFaktum(Long faktumId);

    String hentBehandlingsIdTilFaktum(Long faktumId);

    List<Faktum> hentSystemFaktumList(Long soknadId, String key);

    void settSistLagretTidspunkt(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    void slettSoknad(long soknadId);

    String hentSoknadType(Long soknadId);

    Boolean isVedleggPaakrevd(Long soknadId, SoknadVedlegg soknadVedlegg);

    void settDelstegstatus(Long soknadId, DelstegStatus status);
    void settDelstegstatus(String behandlingsId, DelstegStatus status);

    void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet);

    List<Faktum> hentBarneFakta(Long soknadId, Long faktumId);

    void populerFraStruktur(WebSoknad soknad);

    Optional<WebSoknad> hentEttersendingMedBehandlingskjedeId(String behandlingsId);

    Faktum hentFaktumMedKey(Long soknadId, String faktumKey);
}
