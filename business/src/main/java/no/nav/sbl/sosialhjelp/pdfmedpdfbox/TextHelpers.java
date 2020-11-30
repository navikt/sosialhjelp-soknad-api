package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class TextHelpers {

    @Inject
    public NavMessageSource navMessageSource;

    @Inject
    private KodeverkService kodeverkService;

    public void setNavMessageSource(NavMessageSource navMessageSource) {
        this.navMessageSource = navMessageSource;
    }

    public void setAdressekodeverk(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
    }

    public String fulltNavnForLand(String landForkortelse) {
        if (landForkortelse == null || landForkortelse.equals("???") || landForkortelse.equalsIgnoreCase("YYY")) {
            return "Vi har ikke opplysninger om ditt statsborgerskap";
        } else if (landForkortelse.equalsIgnoreCase("XXX") || landForkortelse.equalsIgnoreCase("XXA")){
            return "Statsløs";
        }

        return kodeverkService.getLand(landForkortelse);
    }
}
