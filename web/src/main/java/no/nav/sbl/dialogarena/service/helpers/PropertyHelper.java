package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.PropertyAware;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PropertyHelper extends RegistryAwareHelper<Object> {
    @Override
    public String getNavn() {
        return "property";
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer verdien til gitt property på modellen i context, gitt at den er propertyaware";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.context.model() instanceof PropertyAware) {
            return ((PropertyAware) options.context.model()).property((String) context);
        }
        return "";
    }
}
