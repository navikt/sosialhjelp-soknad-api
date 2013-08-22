package no.nav.sbl.dialogarena.dokumentinnsending.service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BrukerBehandlingServiceIntegrationMock implements BrukerBehandlingServiceIntegration {

    @Inject
    private DokumentServiceMock dokumentService;

    @Override
    public String opprettDokumentBehandling(String hovedskjemaId, List<String> vedleggsIder, boolean erEttersending) {
        return "0";
    }

    @Override
    public void oppdaterBrukerBehandling(String behandlingsId, String brukerIdent) {
    }

    @Override
    public List<String> hentBrukerBehandlingIder(String aktorId) {
        return new ArrayList<>();
    }

    @Override
    public void sendBrukerBehandling(String behandlingsId, String journalFoerendeEnhet) {
        dokumentService.sendHenvendelse(behandlingsId, journalFoerendeEnhet);
    }

    @Override
    public Boolean ping() {
        return true;
    }
}
