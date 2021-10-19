package no.nav.sosialhjelp.soknad.consumer.svarut;

import no.ks.fiks.svarut.klient.model.Forsendelse;
import no.ks.fiks.svarut.klient.model.ForsendelsesId;

import java.io.InputStream;
import java.util.Map;

public interface SvarUtConsumer {
    ForsendelsesId sendForsendelse(Forsendelse forsendelse, Map<String, InputStream> data);
    void ping();
}
