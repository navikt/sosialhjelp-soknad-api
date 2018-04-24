package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class VisInformasjonOmPerioderHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "visInformasjonOmPerioder";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        if (ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.contains(soknad.getskjemaNummer())) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om en søknad er en førstegangssøknad for foreldrepenger";
    }
}
