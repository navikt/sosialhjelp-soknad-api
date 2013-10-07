package no.nav.sbl.dialogarena;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public class InMemorySoknadInnsendingRepository implements SoknadInnsendingRepository{

	private Map<Long, WebSoknad> soknader = new HashMap<Long, WebSoknad>();
	
	@Override
	public void lagre(long soknadId, String nokkel, String verdi) {

	}

	@Override
	public WebSoknad hentSoknad(long soknadId) {
		return soknader.get(soknadId);
	}

	@Override
	public Long startSoknad(String type) {
		WebSoknad webSoknad = new WebSoknad();
		long nextId = getNextId();
		webSoknad.setSoknadId(nextId);
		webSoknad.setBrukerBehandlingId(new UUID(1, 100000).toString());
		webSoknad.setGosysId(type);
		soknader.put(nextId, webSoknad);
		return nextId;
	}
	
	
	@Override
	public void sendSoknad(long soknadId) {
		System.out.println("Send søknad skal sette en søknad i rikig status");
	}

	@Override
	public void slettSoknad(long soknadId) {
		System.out.println("S før: " + soknader.size());
		soknader.remove(soknadId);
		System.out.println("S etter: " + soknader.size());
	}

	private long getNextId() {
		if (soknader == null) { 
			return 1;
		} else {
			return soknader.size() + 1;
		}
	}
	
}
