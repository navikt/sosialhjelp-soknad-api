package no.nav.sosialhjelp.soknad.consumer.svarut;

import no.ks.fiks.svarut.klient.SvarUtKlientApi;
import no.ks.fiks.svarut.klient.model.Forsendelse;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class SvarUtService {

    private final SvarUtKlientApi svarUtKlientApi;

    public SvarUtService(SvarUtKlientApi svarUtKlientApi) {
        this.svarUtKlientApi = svarUtKlientApi;
    }

    public String send(Forsendelse forsendelse, Map<String, InputStream> filnavnInputStreamMap) {
        var forsendelseId = svarUtKlientApi.sendForsendelse(forsendelse, filnavnInputStreamMap);
        return forsendelseId.getId().toString();
    }
}
