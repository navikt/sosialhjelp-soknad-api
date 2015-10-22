package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class HentLandHelper extends RegistryAwareHelper<String> {

    @Inject
    private Kodeverk kodeverk;

    @Override
    public String getNavn() {
        return "hentLand";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter land fra Kodeverk basert på landkode.";
    }

    @Override
    public CharSequence apply(String landkode, Options options) throws IOException {
        return kodeverk.getLand(landkode);
    }
}