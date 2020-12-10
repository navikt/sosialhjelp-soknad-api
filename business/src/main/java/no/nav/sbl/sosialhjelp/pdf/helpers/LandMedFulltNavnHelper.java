package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata.PDL_STATSLOS;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata.PDL_UKJENT_STATSBORGERSKAP;

@Component
public class LandMedFulltNavnHelper extends RegistryAwareHelper<String>{

    @Inject
    private KodeverkService kodeverkService;

    @Override
    public String getNavn() {
        return "landMedFulltNavn";
    }


    public LandMedFulltNavnHelper() {

    }

    public LandMedFulltNavnHelper(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
    }


    @Override
    public String getBeskrivelse() {
        return "Returnerer fullt navn på land som sendes inn med trebokstav-forkortelse ifølge ISO 3166";
    }

    @Override
    public CharSequence apply(String landForkortelse, Options options) {
        if (landForkortelse == null || landForkortelse.equals("???") || landForkortelse.equalsIgnoreCase("YYY") || landForkortelse.equalsIgnoreCase(PDL_UKJENT_STATSBORGERSKAP)) {
            return "Vi har ikke opplysninger om ditt statsborgerskap";
        } else if (landForkortelse.equalsIgnoreCase(PDL_STATSLOS) || landForkortelse.equalsIgnoreCase("XXA")){
            return "Statsløs";
        }

        return kodeverkService.getLand(landForkortelse);
    }
}
