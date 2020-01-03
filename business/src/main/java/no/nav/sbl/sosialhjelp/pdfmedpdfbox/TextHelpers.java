package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class TextHelpers {

    @Inject
    public NavMessageSource navMessageSource;

    @Inject
    private Adressekodeverk adressekodeverk;

    TextHelpers(NavMessageSource navMessageSource, Adressekodeverk adressekodeverk) {
        this.navMessageSource = navMessageSource;
        this.adressekodeverk = adressekodeverk;
    }

    public String fulltNavnForLand(String landForkortelse) {
        if (landForkortelse == null || landForkortelse.equals("???") || landForkortelse.equalsIgnoreCase("YYY")) {
            return "Vi har ikke opplysninger om ditt statsborgerskap";
        } else if (landForkortelse.equalsIgnoreCase("XXX") || landForkortelse.equalsIgnoreCase("XXA")){
            return "Statsløs";
        }

        return adressekodeverk.getLand(landForkortelse);
    }
}
