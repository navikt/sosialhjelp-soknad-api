package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import org.springframework.stereotype.Service;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.MetricsUtils.getProsent;

@Service
public class SoknadMetricsService {

    public void startSoknad(boolean isEttersendelse) {
        reportSoknad("soknad.start", isEttersendelse);
    }

    public void sendSoknad(boolean isEttersendelse) {
        reportSoknad("soknad.send", isEttersendelse);
    }

    public void avbruttSoknad(boolean isEttersendelse) {
        reportSoknad("soknad.avbrutt", isEttersendelse);
    }

    private void reportSoknad(String name, boolean isEttersendelse) {
        String soknadstype = (isEttersendelse ? "ettersending." : "") + SOKNAD_TYPE_PREFIX;

        Event event = MetricsFactory.createEvent(name);
        event.addFieldToReport("soknadstype", soknadstype);
        event.report();
    }

    public void reportSoknadVedlegg(boolean isEttersendelse, int totaltAntall, int antallInnsendt, int anntallLevertTidligere, int antallIkkeLevert) {
        String sendtype = (isEttersendelse ? "ettersendelse" : "soknad");

        Event event = MetricsFactory.createEvent("digisos.vedleggskrav." + sendtype);
        event.addFieldToReport("antall.totalt", totaltAntall);
        event.addFieldToReport("antall.innsendt", antallInnsendt);
        event.addFieldToReport("antall.levertTidligere", anntallLevertTidligere);
        event.addFieldToReport("antall.ikkeLevert", antallIkkeLevert);

        event.addFieldToReport("prosent.innsendt", getProsent(antallInnsendt, totaltAntall));
        event.addFieldToReport("prosent.levertTidligere", getProsent(anntallLevertTidligere, totaltAntall));
        event.addFieldToReport("prosent.ikkeLevert", getProsent(antallIkkeLevert, totaltAntall));
        event.report();
    }

}
