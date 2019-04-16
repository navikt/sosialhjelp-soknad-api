package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static no.nav.sbl.dialogarena.common.Spraak.NORSK_BOKMAAL;

@Component
public class HvisBarneutgiftHelper extends RegistryAwareHelper<Object>{

    @Inject
    private HentSvaralternativerHelper hentSvaralternativerHelper;

    public static final String NAVN = "hvisBarneutgift";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        Set<String> barneutgiftTyper =  hentSvaralternativerHelper.findChildPropertySubkeys("utgifter.barn.true.utgifter", NORSK_BOKMAAL);
        barneutgiftTyper.add("barnebidrag");

        if (key != null && barneutgiftTyper.contains(key.toString())){
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
        return "Sjekker om en streng er av typen barneutgift";
    }
}
