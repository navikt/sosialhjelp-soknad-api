package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

@Component
public class HvisIngenSynligeBarneFaktaHelper extends RegistryAwareHelper<List<? extends OppsummeringsFaktum>> {

    private Predicate<OppsummeringsFaktum> faktaErSynligPredicate(){
        return oppsummeringsFaktum -> oppsummeringsFaktum.erSynlig();
    }

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
        boolean ingenSynlige = fakta.stream().noneMatch(faktaErSynligPredicate());
        return ingenSynlige ? options.fn() : options.inverse();
    }
}
