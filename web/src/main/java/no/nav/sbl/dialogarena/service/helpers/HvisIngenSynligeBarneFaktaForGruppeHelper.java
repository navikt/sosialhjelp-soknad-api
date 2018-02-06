package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

@Component
public class HvisIngenSynligeBarneFaktaForGruppeHelper extends RegistryAwareHelper<List<? extends OppsummeringsFaktum>> {

    private Predicate<OppsummeringsFaktum> faktaErSynligPredicate(boolean utvidet){
        return faktum -> utvidet ? faktum.erSynlig() : (faktum.erSynlig() && !faktum.struktur.getKunUtvidet());
    }

    @Override
    public String getNavn() {
        return "hvisIngenSynligeBarneFaktaForGruppe";
    }

    @Override
    public String getBeskrivelse() {
        return "For gruppe-template brukt for sosialhjelp, der vi ønsker utvidet definisjon av hva som er synlige barnefakta" +
                " mtp utvidet søknad";
    }

    @Override
    public CharSequence apply(List<? extends OppsummeringsFaktum> fakta, Options options) throws IOException {
        boolean utvidet = options.param(0);
        boolean ingenSynlige = fakta.stream().noneMatch(faktaErSynligPredicate(utvidet));
        return ingenSynlige ? options.fn() : options.inverse();
    }
}
