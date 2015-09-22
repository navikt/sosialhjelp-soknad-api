package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

@Component
public class KortDatoHelper extends RegistryAwareHelper<String> {
    public static final String NAVN = "kortDato";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Formatterer en datostreng på formatet yyyy-mm-dd til dd.mm.aaaa";
    }

    @Override
    public CharSequence apply(String value, Options options) throws IOException {
        String[] datoSplit = split(value, "-");
        reverse(datoSplit);
        return join(datoSplit, ".");
    }
}
