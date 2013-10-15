package no.nav.sbl.dialogarena.websoknad.service;

import java.util.List;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public interface SendSoknadService {

	Long startSoknad(String navSoknadId);

	WebSoknad hentSoknad(long soknadId);

	void lagreSoknadsFelt(long soknadId, String key, String value);

	void sendSoknad(long soknadId);

	List<Long> hentMineSoknader(String aktorId);

	void avbrytSoknad(Long soknadId);

}