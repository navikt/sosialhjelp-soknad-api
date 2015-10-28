package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Predicate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HvisFlereErTrueHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "hvisFlereErTrue";
    }

    @Override
    public String getBeskrivelse() {
        return "Finner alle fakta med key som begynner med teksten som sendes inn og teller om antallet med verdien true er større enn tallet som sendes inn.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        Integer grense = Integer.parseInt((String) options.param(0));

        WebSoknad soknad = finnWebSoknad(options.context);
        List<Faktum> fakta = soknad.getFaktaSomStarterMed(key);

        int size = on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                String value = faktum.getValue();
                return value != null && value.equals("true");
            }
        }).collect().size();


        if (size > grense) {
            return options.fn();
        } else {
            return options.inverse();
        }
    }
}
