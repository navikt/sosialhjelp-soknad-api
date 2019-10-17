package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

@Component
public class FinnSaksStatusHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "finnSaksStatus";
    }

    @Override
    public String getBeskrivelse() {
        return "Finner ut hva som er rett saks status å vise.";
    }

    @Override
    public CharSequence apply(String status, Options options) {
        if (status == null) {
            return "";
        }

        if(status.equalsIgnoreCase("VEDTATT")) {
            if(options.params.length > 0) {
                return String.valueOf(options.params[0]);
            }
            return "Vedtatt";
        }
        return "Under behandling";
    }
}
