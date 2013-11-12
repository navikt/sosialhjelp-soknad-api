package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.List;

public interface SendSoknadService {

	Long startSoknad(String navSoknadId);

	WebSoknad hentSoknad(long soknadId);

	void lagreSoknadsFelt(long soknadId, String key, String value);

	void sendSoknad(long soknadId);

	List<Long> hentMineSoknader(String aktorId);

	void avbrytSoknad(Long soknadId);

}