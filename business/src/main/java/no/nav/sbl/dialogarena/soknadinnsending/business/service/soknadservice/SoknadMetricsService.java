package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import org.springframework.stereotype.Service;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;

@Service
public class SoknadMetricsService {

    private static final long RAPPORTERINGS_RATE = 15 * 60 * 1000; // hvert kvarter

    public void startetSoknad(boolean erEttersending) {
        rapporterSoknad("soknad.start", erEttersending);
    }

    public void sendtSoknad(boolean erEttersending) {
        rapporterSoknad("soknad.send", erEttersending);
    }

    public void avbruttSoknad(boolean erEttersending) {
        rapporterSoknad("soknad.avbrutt", erEttersending);
    }

    private void rapporterSoknad(String name, boolean erEttersending) {
        String soknadstype = (erEttersending ? "ettersending." : "") + SOKNAD_TYPE_PREFIX;

        Event event = MetricsFactory.createEvent(name);
        event.addFieldToReport("soknadstype", soknadstype);
        event.report();
    }
}
