package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata.PDL_STATSLOS;
import static no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata.PDL_UKJENT_STATSBORGERSKAP;

@Component
public class TextHelpers {

    @Inject
    public NavMessageSource navMessageSource;

    @Inject
    private KodeverkService kodeverkService;

    public void setNavMessageSource(NavMessageSource navMessageSource) {
        this.navMessageSource = navMessageSource;
    }

    public void setKodeverkService(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
    }

    public String fulltNavnForLand(String landForkortelse) {
        if (landForkortelse == null || landForkortelse.equals("???") || landForkortelse.equalsIgnoreCase("YYY") || landForkortelse.equalsIgnoreCase(PDL_UKJENT_STATSBORGERSKAP)) {
            return "Vi har ikke opplysninger om ditt statsborgerskap";
        } else if (landForkortelse.equalsIgnoreCase(PDL_STATSLOS) || landForkortelse.equalsIgnoreCase("XXA")){
            return "Statsløs";
        }

        return kodeverkService.getLand(landForkortelse);
    }
}
