package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

public interface EttersendingService {

    interface Create {

    }

    interface Read {

    }

    interface Update {

    }

    WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingskjedeId);

    Long startEttersending(String behandingId, String fodselsnummer);
}
