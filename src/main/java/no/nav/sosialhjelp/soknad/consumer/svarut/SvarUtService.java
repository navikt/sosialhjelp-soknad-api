package no.nav.sosialhjelp.soknad.consumer.svarut;

import no.ks.fiks.svarut.klient.model.Forsendelse;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class SvarUtService {

    private final SvarUtConsumer svarUtConsumer;

    public SvarUtService(SvarUtConsumer svarUtConsumer) {
        this.svarUtConsumer = svarUtConsumer;
    }

    public String send(Forsendelse forsendelse, Map<String, InputStream> filnavnInputStreamMap) {
        var forsendelseId = svarUtConsumer.sendForsendelse(forsendelse, filnavnInputStreamMap);
        return forsendelseId.getId().toString();
    }

}
