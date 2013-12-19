package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.List;

public interface SoknadRepository {

    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);

    WebSoknad hentSoknadMedData(Long id);

    List<Faktum> hentAlleBrukerData(Long soknadId);

    void avslutt(WebSoknad soknad);

    void avbryt(Long soknad);

    List<WebSoknad> hentListe(String aktorId);

    Long lagreFaktum(long soknadId, Faktum faktum);

    WebSoknad hentMedBehandlingsId(String behandlingsId);

    String opprettBehandling();

    Faktum hentFaktum(Long soknadId, Long faktumId);

    Faktum hentSystemFaktum(Long soknadId, String key,
                            String systemregistrertFaktum);


    void endreInnsendingsValg(Long soknadId, Long faktumId, Faktum.Status innsendingsvalg);

    void settSistLagretTidspunkt(Long soknadId);

    void slettBrukerFaktum(Long soknadId, Long faktumId);

    void slettSoknadsFelt(Long soknadId, Long faktumId);

    void slettBarnSoknadsFelt(Long soknadId);
}
