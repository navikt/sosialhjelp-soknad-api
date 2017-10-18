package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

@Component
public class HvisNoenAvkryssetBarneFaktaHelper extends RegistryAwareHelper<List<? extends OppsummeringsFaktum>> {

    private Predicate<OppsummeringsFaktum> faktaErAvhuketPredicate(){
        return oppsummeringsFaktum -> oppsummeringsFaktum.value().equals("true");
    }

    @Override
    public String getNavn() {
        return "hvisNoenAvkryssetBarneFakta";
    }

    @Override
    public String getBeskrivelse() {
        return "For bruk i generisk oppsummering, undersøker innsendt liste over fakta og ser om noen er avhuket.";
    }

    @Override
    public CharSequence apply(List<? extends OppsummeringsFaktum> fakta, Options options) throws IOException {
        boolean ingenSynlige = fakta.stream().anyMatch(faktaErAvhuketPredicate());
        return ingenSynlige ? options.fn() : options.inverse();
    }
}
