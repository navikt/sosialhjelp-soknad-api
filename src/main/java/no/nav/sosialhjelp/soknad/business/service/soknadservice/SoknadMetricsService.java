package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sosialhjelp.metrics.Event;
import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.PersonAlder;
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.isVedleggskravAnnet;
import static no.nav.sosialhjelp.soknad.business.util.MetricsUtils.getProsent;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SoknadMetricsService {

    private static final Logger log = getLogger(SoknadMetricsService.class);

    public void reportSendSoknadMetrics(String eier, SoknadUnderArbeid soknadUnderArbeid, List<SoknadMetadata.VedleggMetadata> vedleggList) {
        reportSendSoknad(soknadUnderArbeid.erEttersendelse());
        countAndreportVedleggskrav(soknadUnderArbeid.erEttersendelse(), vedleggList);
        reportAlder(eier, soknadUnderArbeid);
    }

    public void reportStartSoknad(boolean isEttersendelse) {
        reportSoknad("soknad.start", isEttersendelse);
    }

    private void reportSendSoknad(boolean isEttersendelse) {
        reportSoknad("soknad.send", isEttersendelse);
    }

    public void reportAvbruttSoknad(boolean isEttersendelse) {
        reportSoknad("soknad.avbrutt", isEttersendelse);
    }

    private void reportSoknad(String name, boolean isEttersendelse) {
        String soknadstype = (isEttersendelse ? "ettersending." : "") + SOKNAD_TYPE_PREFIX;

        Event event = MetricsFactory.createEvent(name);
        event.addFieldToReport("soknadstype", soknadstype);
        event.report();
    }

    void reportVedleggskrav(boolean isEttersendelse, int totaltAntall, int antallInnsendt, int antallLevertTidligere, int antallIkkeLevert) {
        String sendtype = (isEttersendelse ? "ettersendelse" : "soknad");

        Event event = MetricsFactory.createEvent("digisos.vedleggskrav");
        event.addTagToReport("sendetype", sendtype);

        event.addFieldToReport("antall.totalt", totaltAntall);
        event.addFieldToReport("antall.innsendt", antallInnsendt);
        event.addFieldToReport("antall.levertTidligere", antallLevertTidligere);
        event.addFieldToReport("antall.ikkeLevert", antallIkkeLevert);

        event.addFieldToReport("prosent.innsendt", getProsent(antallInnsendt, totaltAntall));
        event.addFieldToReport("prosent.levertTidligere", getProsent(antallLevertTidligere, totaltAntall));
        event.addFieldToReport("prosent.ikkeLevert", getProsent(antallIkkeLevert, totaltAntall));
        event.report();
    }

    void countAndreportVedleggskrav(boolean isEttersendelse, List<SoknadMetadata.VedleggMetadata> vedleggList) {
        int antallInnsendt = 0;
        int antallLevertTidligere = 0;
        int antallIkkeLevert = 0;
        int totaltAntall = 0;

        for (SoknadMetadata.VedleggMetadata vedlegg : vedleggList) {
            if (!isVedleggskravAnnet(vedlegg)) {
                totaltAntall++;
                switch (vedlegg.status) {
                    case LastetOpp:
                        antallInnsendt++;
                        break;
                    case VedleggAlleredeSendt:
                        antallLevertTidligere++;
                        break;
                    case VedleggKreves:
                        antallIkkeLevert++;
                        break;
                }
            }
        }

        reportVedleggskrav(
                isEttersendelse,
                totaltAntall,
                antallInnsendt,
                antallLevertTidligere,
                antallIkkeLevert
        );
    }

    private static void reportAlder(String eier, SoknadUnderArbeid soknadUnderArbeid) {
        if (!soknadUnderArbeid.erEttersendelse() && !MockUtils.isTillatMockRessurs()) {
            int age = new PersonAlder(eier).getAlder();
            if (age > 0 && age < 30) {
                log.info("DIGISOS-1164: UNDER30 - Soknad sent av bruker med alder: {}", age);
            } else {
                log.info("DIGISOS-1164: OVER30 - Soknad sent av bruker med alder: {}", age);
            }
        }
    }
}
