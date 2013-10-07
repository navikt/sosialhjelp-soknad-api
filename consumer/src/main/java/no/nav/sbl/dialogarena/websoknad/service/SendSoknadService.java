package no.nav.sbl.dialogarena.websoknad.service;

import java.util.List;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public interface SendSoknadService {

	abstract Long startSoknad(String navSoknadId);

	abstract WebSoknad hentSoknad(long soknadId);

	abstract void lagreSoknadsFelt(long soknadId, String key,
			String value);

	abstract void sendSoknad(long soknadId);

	abstract List<Long> hentMineSoknader(String aktorId);

	abstract void avbrytSoknad(Long soknadId);

}