package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;

@Component
public class HvisBoutgiftHelper extends RegistryAwareHelper<Object>{

    @Inject
    private HentSvaralternativerHelper hentSvaralternativerHelper;

    public static final String NAVN = "hvisBoutgift";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        final Set<String> boutgiftTyper =  hentSvaralternativerHelper.findChildPropertySubkeys("utgifter.boutgift.true.type", SPRAK);

        if (key != null && boutgiftTyper.contains(key.toString())){
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
        return "Sjekker om en streng er av typen boutgift";
    }
}
