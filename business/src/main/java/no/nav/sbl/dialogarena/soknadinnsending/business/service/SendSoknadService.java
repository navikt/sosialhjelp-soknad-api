package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import java.util.List;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

public interface SendSoknadService {

	Long startSoknad(String navSoknadId);

	WebSoknad hentSoknad(long soknadId);

	Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum);

	Faktum lagreSystemSoknadsFelt(Long soknadId, Faktum faktum);
	
	void sendSoknad(long soknadId);

	List<Long> hentMineSoknader(String aktorId);

	void avbrytSoknad(Long soknadId);

}