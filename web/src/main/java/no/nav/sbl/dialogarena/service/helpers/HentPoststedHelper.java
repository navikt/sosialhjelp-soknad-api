package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class HentPoststedHelper extends RegistryAwareHelper<String> {

    @Inject
    private Kodeverk kodeverk;

    @Override
    public String getNavn() {
        return "hentPoststed";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter poststed for et postnummer fra kodeverk";
    }

    @Override
    public CharSequence apply(String postnummer, Options options) throws IOException {
        return kodeverk.getPoststed(postnummer);
    }
}
