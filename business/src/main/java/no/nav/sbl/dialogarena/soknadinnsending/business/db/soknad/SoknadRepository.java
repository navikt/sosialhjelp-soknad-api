package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;


import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;

import java.util.List;
import java.util.Map;

public interface SoknadRepository {

    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);
    WebSoknad hentSoknad(String behandlingsId);

    WebSoknad hentSoknadMedData(Long id);
    WebSoknad hentSoknadMedVedlegg(String behandlingsId);

    List<Faktum> hentAlleBrukerData(String behandlingsId);

    Optional<WebSoknad> plukkSoknadTilMellomlagring();

    void leggTilbake(WebSoknad webSoknad);
    
    Long oppdaterFaktum(Faktum faktum);
    Long opprettFaktum(long soknadId, Faktum faktum, Boolean systemFaktum);
    Long opprettFaktum(long soknadId, Faktum faktum);
    Long oppdaterFaktum(Faktum faktum, Boolean systemFaktum);

    void batchOpprettTommeFakta(List<Faktum> fakta);
    List<Long> hentLedigeFaktumIder(int antall);

    Faktum hentFaktum(Long faktumId);

    String hentBehandlingsIdTilFaktum(Long faktumId);

    List<Faktum> hentSystemFaktumList(Long soknadId, String key);

    void settSistLagretTidspunkt(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    void slettSoknad(long soknadId);

    String hentSoknadType(Long soknadId);

    Boolean isVedleggPaakrevd(Long soknadId, VedleggForFaktumStruktur vedleggForFaktumStruktur);

    void settDelstegstatus(Long soknadId, DelstegStatus status);
    void settDelstegstatus(String behandlingsId, DelstegStatus status);

    void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet);

    List<Faktum> hentBarneFakta(Long soknadId, Long faktumId);

    void populerFraStruktur(WebSoknad soknad);

    Optional<WebSoknad> hentEttersendingMedBehandlingskjedeId(String behandlingsId);

    Faktum hentFaktumMedKey(Long soknadId, String faktumKey);

    Map<String, String> hentDatabaseStatus();
}
