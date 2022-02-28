//package no.nav.sosialhjelp.soknad.business.db.repositories.oppgave;
//
//import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave;
//
//import java.util.Map;
//import java.util.Optional;
//
//public interface OppgaveRepository {
//
//    void opprett(Oppgave oppgave);
//
//    Optional<Oppgave> hentNeste();
//
//    Optional<Oppgave> hentOppgave(String behandlingsId);
//
//    void oppdater(Oppgave oppgave);
//
//    Map<String, Integer> hentStatus();
//
//    int retryOppgaveStuckUnderArbeid();
//
//    void slettOppgave(String behandlingsId);
//}
