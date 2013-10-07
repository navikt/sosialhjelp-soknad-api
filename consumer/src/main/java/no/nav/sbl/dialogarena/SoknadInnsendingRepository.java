package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public interface SoknadInnsendingRepository {

	public void lagre(long soknadId, String nokkel, String verdi);

	public WebSoknad hentSoknad(long soknadId);
	
	public Long startSoknad(String type);
	
	public void slettSoknad(long soknadId);
	
	public void sendSoknad(long soknadId);
	
}
