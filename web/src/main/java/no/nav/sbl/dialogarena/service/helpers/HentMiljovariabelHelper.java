package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Miljovariabler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class HentMiljovariabelHelper extends RegistryAwareHelper<String> {
    @Inject
    private Miljovariabler informasjonService;

    public static final String NAVN = "hentMiljovariabel";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Finner miljovariabel fra key";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        return informasjonService.hentMiljovariabler().get(key);
    }
}