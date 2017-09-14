package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.apache.commons.collections15.Predicate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.*;

@Component
public class HvisIngenSynligeBarneFaktaHelper extends RegistryAwareHelper<List<? extends OppsummeringsFaktum>> {

    private Predicate<OppsummeringsFaktum> faktaErSynligPredicate = new Predicate<OppsummeringsFaktum>() {
        @Override
        public boolean evaluate(OppsummeringsFaktum oppsummeringsFaktum) {
            return oppsummeringsFaktum.erSynlig();
        }
    };


    @Override
    public String getNavn() {
        return "hvisIngenSynligeBarneFakta";
    }

    @Override
    public String getBeskrivelse() {
        return "For bruk i generisk oppsummering, undersøker innsendt liste over fakta og ser om alle er skjult.";
    }

    @Override
    public CharSequence apply(List<? extends OppsummeringsFaktum> fakta, Options options) throws IOException {
        boolean ingenSynlige = on(fakta).filter(faktaErSynligPredicate).isEmpty();

        return ingenSynlige ? options.fn() : options.inverse();
    }
}
