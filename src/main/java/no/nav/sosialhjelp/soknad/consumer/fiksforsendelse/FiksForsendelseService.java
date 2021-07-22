package no.nav.sosialhjelp.soknad.consumer.fiksforsendelse;

import no.ks.fiks.svarut.klient.SvarUtKlientApi;
import no.ks.fiks.svarut.klient.model.Forsendelse;

public class FiksForsendelseService {

    private final SvarUtKlientApi svarUtKlientApi;

    public FiksForsendelseService(SvarUtKlientApi svarUtKlientApi) {
        this.svarUtKlientApi = svarUtKlientApi;
    }

    public void send(Forsendelse forsendelse) {
        svarUtKlientApi.sendForsendelse(forsendelse, null);
    }
}
