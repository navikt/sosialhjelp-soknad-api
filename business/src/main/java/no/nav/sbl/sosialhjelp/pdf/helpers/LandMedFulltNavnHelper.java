package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

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
    public CharSequence apply(String landForkortelse, Options options) throws IOException {
        if (landForkortelse == null || landForkortelse.equals("???")) {
            return "Vi har ikke opplysninger om ditt statsborgerskap";
        } else if (landForkortelse.equals("XXX") || landForkortelse.equals("xxx")){
            return "Statsløs";
        }

        return adressekodeverk.getLand(landForkortelse);
    }
}
