package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class SoknadMetricsService {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public void startetSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.start", skjemanummer, erEttersending);
    }

    public void sendtSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.send", skjemanummer, erEttersending);
    }

    public void avbruttSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.avbrutt", skjemanummer, erEttersending);
    }

    private void rapporterSoknad(String name, String skjemanummer, boolean erEttersending) {
        String soknadstype = getSoknadstype(skjemanummer, erEttersending);

        Event event = MetricsFactory.createEvent(name);
        event.addFieldToReport("soknadstype", soknadstype);
        event.report();
    }

    private String getSoknadstype(String skjemanummer, boolean erEttersending) {
        String type = kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer).getSoknadTypePrefix();
        return (erEttersending ? "ettersending." : "") + type;
    }
}
