package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.*;

@Component
public class ForBarnefaktaHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "forBarnefakta";
    }

    @Override
    public String getBeskrivelse() {
        return "Itererer over alle fakta som har den gitte keyen og parentfaktum satt til nærmeste faktum oppover i context.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum parentFaktum = finnFaktum(options.context);
        List<Faktum> fakta = soknad.getFaktaMedKeyOgParentFaktum(key, parentFaktum.getFaktumId());
        if (fakta.isEmpty()) {
            return options.inverse();
        } else {
            return lagItererbarRespons(options, fakta);
        }
    }
}
