package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

public interface SoknadInnsendingRepository {

	Long lagre(long soknadId, String nokkel, String verdi);

	WebSoknad hentSoknad(long soknadId);
	
	Long startSoknad(String type);
	
	void slettSoknad(long soknadId);
	
	void sendSoknad(long soknadId);
	
	Faktum hentFaktum(Long soknadId, Long faktumId);
	
}
