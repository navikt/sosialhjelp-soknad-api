package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import static no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak.Vedtaksstatus.AVVIST;
import static no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak.Vedtaksstatus.INNVILGET;

@Component
public class FinnSaksstatusHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "finnSaksstatus";
    }

    @Override
    public String getBeskrivelse() {
        return "Finner ut hva som er rett saksstatus å vise.";
    }

    @Override
    public CharSequence apply(String status, Options options) {
        if (status == null) {
            return "";
        }

        if(status.equalsIgnoreCase("VEDTATT")) {
            if(options.params.length > 1) {
                if (options.params[1] == INNVILGET || "INNVILGET".equalsIgnoreCase(options.params[1].toString())) {
                    return "Innvilget: " + options.params[0];
                }
                if (options.params[1] == AVVIST || "AVVIST".equalsIgnoreCase(options.params[1].toString())) {
                    return "Avvist: " + options.params[0];
                }
                return "Avslag: " + options.params[0];
            }
            return "Vedtatt";
        }
        return "Under behandling";
    }
}
