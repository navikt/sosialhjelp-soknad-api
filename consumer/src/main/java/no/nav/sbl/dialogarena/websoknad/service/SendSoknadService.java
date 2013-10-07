package no.nav.sbl.dialogarena.websoknad.service;

import java.util.List;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

public interface SendSoknadService {

	public abstract Long startSoknad(String navSoknadId);

	public abstract WebSoknad hentSoknad(long soknadId);

	public abstract void lagreSoknadsFelt(long soknadId, String key,
			String value);

	public abstract void sendSoknad(long soknadId);

	public abstract List<Long> hentMineSoknader(String aktorId);

	public abstract void avbrytSoknad(Long soknadId);

}