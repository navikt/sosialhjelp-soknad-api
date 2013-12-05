package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import java.util.List;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

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


}
