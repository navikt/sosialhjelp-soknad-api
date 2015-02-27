package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

public interface EttersendingService {

    public interface Create {

    }

    public interface Read {

    }

    public interface Update {

    }



    WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingskjedeId);

    Long startEttersending(String behandingId);

    void avbrytSoknad(Long soknadId);

}
