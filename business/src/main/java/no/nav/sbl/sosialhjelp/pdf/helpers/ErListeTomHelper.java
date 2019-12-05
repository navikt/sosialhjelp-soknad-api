package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class ErListeTomHelper extends RegistryAwareHelper<Collection<Object>> {

    @Override
    public String getNavn() {
        return "erListeTom";
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer sant dersom listen er tom";
    }

    @Override
    public CharSequence apply(Collection<Object> liste, Options options) throws IOException {
        if (liste == null) {
            return options.fn(this);
        }

        if(liste.size() > 0) {
            return options.inverse(this);
        }
        return options.fn(this);
    }
}
