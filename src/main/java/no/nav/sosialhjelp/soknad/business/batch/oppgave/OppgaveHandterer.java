package no.nav.sosialhjelp.soknad.business.batch.oppgave;

public interface OppgaveHandterer {
    void leggTilOppgave(String behandlingsId, String eier);
}
