package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public interface SoknadInnsendingRepository {

	void lagre(long soknadId, String nokkel, String verdi);

	WebSoknad hentSoknad(long soknadId);
	
	Long startSoknad(String type);
	
	void slettSoknad(long soknadId);
	
	void sendSoknad(long soknadId);
	
}
