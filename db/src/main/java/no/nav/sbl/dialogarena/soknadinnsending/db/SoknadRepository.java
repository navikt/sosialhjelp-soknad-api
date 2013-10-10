package no.nav.sbl.dialogarena.soknadinnsending.db;


import java.util.List;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public interface SoknadRepository {
	
    Long opprettSoknad(WebSoknad soknad);

    WebSoknad hentSoknad(Long id);

    WebSoknad hentSoknadMedData(Long id);

    List<Faktum> hentAlleBrukerData(Long soknadId);
    
	void avslutt(WebSoknad soknad);

	void avbryt(WebSoknad soknad);

	List<WebSoknad> hentListe(String aktorId);

	void lagreFaktum(long soknadId, Faktum faktum);

	WebSoknad hentMedBehandlingsId(String behandlingsId);

	String opprettBehandling();


}
