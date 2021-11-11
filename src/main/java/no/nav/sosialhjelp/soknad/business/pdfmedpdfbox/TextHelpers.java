package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.stereotype.Component;

import static no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata.PDL_STATSLOS;
import static no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata.PDL_UKJENT_STATSBORGERSKAP;

@Component
public class TextHelpers {

    public final NavMessageSource navMessageSource;
    private final KodeverkService kodeverkService;

    public TextHelpers(NavMessageSource navMessageSource, KodeverkService kodeverkService) {
        this.navMessageSource = navMessageSource;
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
