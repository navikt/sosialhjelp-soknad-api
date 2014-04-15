package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

public interface EttersendingService {
    WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingskjedeId);

    Long startEttersending(String behandingId);

    void sendEttersending(Long soknadId, String behandingsId);

    WebSoknad hentEttersendingMedData(String behandlingskjedeId);

    void avbrytSoknad(Long soknadId);
}
