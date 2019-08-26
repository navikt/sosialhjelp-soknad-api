package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class BrukerregistrerteBarnFinnesHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "brukerregistrerteBarnFinnes";

    @Override
    public CharSequence apply(String type, Options options) throws IOException {
        @SuppressWarnings("unchecked") JsonForsorgerplikt forsorgerplikt = (JsonForsorgerplikt) options.context.get("forsorgerplikt");

        Optional<JsonAnsvar> brukerregistrertBarn = forsorgerplikt.getAnsvar().stream()
                .filter(ansvar -> ansvar.getBarn().getKilde().equals(JsonKilde.BRUKER))
                .findFirst();

        if (brukerregistrertBarn.isPresent()) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() { return NAVN; }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om det finnes brukerregistrerte barn";
    }
}
