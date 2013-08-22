package no.nav.sbl.dialogarena.dokumentinnsending.service;

import java.util.List;

public interface BrukerBehandlingServiceIntegration extends OpprettBrukerBehandlingService {

    void oppdaterBrukerBehandling(String behandlingsId, String brukerIdent);

    List<String> hentBrukerBehandlingIder(String aktorId);

    void sendBrukerBehandling(String behandlingsId, String journalFoerendeEnhet);

}
