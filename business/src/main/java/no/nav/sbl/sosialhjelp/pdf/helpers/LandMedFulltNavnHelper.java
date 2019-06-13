package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class LandMedFulltNavnHelper extends RegistryAwareHelper<String>{

    @Inject
    private Adressekodeverk adressekodeverk;

    @Override
    public String getNavn() {
        return "landMedFulltNavn";
    }


    public LandMedFulltNavnHelper() {

    }

    public LandMedFulltNavnHelper(Adressekodeverk adressekodeverk) {
        this.adressekodeverk = adressekodeverk;
    }


    @Override
    public String getBeskrivelse() {
        return "Returnerer fullt navn på land som sendes inn med trebokstav-forkortelse ifølge ISO 3166";
    }

    @Override
    public CharSequence apply(String landForkortelse, Options options) {
        if (landForkortelse == null || landForkortelse.equals("???") || landForkortelse.equals("YYY") || landForkortelse.equals("yyy")) {
            return "Vi har ikke opplysninger om ditt statsborgerskap";
        } else if (landForkortelse.equals("XXX") || landForkortelse.equals("xxx") || landForkortelse.equals("XXA") || landForkortelse.equals("xxa")){
            return "Statsløs";
        }

        return adressekodeverk.getLand(landForkortelse);
    }
}
