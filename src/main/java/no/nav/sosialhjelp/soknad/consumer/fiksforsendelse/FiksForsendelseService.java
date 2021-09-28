package no.nav.sosialhjelp.soknad.consumer.fiksforsendelse;

import no.ks.fiks.svarut.klient.SvarUtKlientApi;
import no.ks.fiks.svarut.klient.model.Forsendelse;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class FiksForsendelseService {

    private final SvarUtKlientApi svarUtKlientApi;

    public FiksForsendelseService(SvarUtKlientApi svarUtKlientApi) {
        this.svarUtKlientApi = svarUtKlientApi;
    }

    public String send(Forsendelse forsendelse, Map<String, InputStream> filnavnInputStreamMap) {
        var forsendelseId = svarUtKlientApi.sendForsendelse(forsendelse, filnavnInputStreamMap);
        return forsendelseId.getId().toString();
    }
}
