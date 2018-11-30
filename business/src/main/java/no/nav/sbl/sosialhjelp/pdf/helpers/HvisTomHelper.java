package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisTomHelper extends RegistryAwareHelper<Object> {

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        if (key != null && !key.toString().isEmpty()) {
            return options.inverse();
        } else {
            return options.fn();
        }
    }

    @Override
    public String getNavn() {
        return "hvisTom";
    }

    @Override
    public String getBeskrivelse() {
        return "Helper for en tom variabel";
    }

}
