package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.MetricsUtils.getProsent;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SoknadMetricsService {

    private static final Logger log = getLogger(SoknadMetricsService.class);

    public void reportSendSoknadMetrics(String eier, SoknadUnderArbeid soknadUnderArbeid, List<SoknadMetadata.VedleggMetadata> vedleggList) {
        reportSendSoknad(soknadUnderArbeid.erEttersendelse());
        reportVedleggskrav(soknadUnderArbeid, vedleggList);
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

    private void reportVedleggskrav(boolean isEttersendelse, int totaltAntall, int antallInnsendt, int anntallLevertTidligere, int antallIkkeLevert) {
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

    private void reportVedleggskrav(SoknadUnderArbeid soknadUnderArbeid, List<SoknadMetadata.VedleggMetadata> vedleggList) {
        int antallInnsendt = 0;
        int anntallLevertTidligere = 0;
        int antallIkkeLevert = 0;

        for (SoknadMetadata.VedleggMetadata vedlegg : vedleggList) {
            switch (vedlegg.status) {
                case LastetOpp:
                    antallInnsendt++;
                    break;
                case VedleggAlleredeSendt:
                    anntallLevertTidligere++;
                    break;
                case VedleggKreves:
                    antallIkkeLevert++;
                    break;
            }
        }

        reportVedleggskrav(
                soknadUnderArbeid.erEttersendelse(),
                vedleggList.size(),
                antallInnsendt,
                anntallLevertTidligere,
                antallIkkeLevert
        );
    }

    private static void reportAlder(String eier, SoknadUnderArbeid soknadUnderArbeid) {
        if (!soknadUnderArbeid.erEttersendelse() && !MockUtils.isTillatMockRessurs()) {
            int age = new PersonAlder(eier).getAlder();
            if (age > 0 && age < 30) {
                log.info("DIGISOS-1164: UNDER30 - Soknad sent av bruker med alder: " + age);
            } else {
                log.info("DIGISOS-1164: OVER30 - Soknad sent av bruker med alder:" + age);
            }
        }
    }
}
