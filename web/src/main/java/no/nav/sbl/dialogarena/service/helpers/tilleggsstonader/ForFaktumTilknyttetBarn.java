package no.nav.sbl.dialogarena.service.helpers.tilleggsstonader;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnFaktum;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;


@Component
public class ForFaktumTilknyttetBarn extends RegistryAwareHelper<String>{

    public static final String NAVN = "forFaktumTilknyttetBarn";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer faktumet tilknyttet barnet i parent-context.";
    }

    @Override
    public CharSequence apply(String faktumKey, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum barneFaktum = finnFaktum(options.context);

        for(Faktum faktum : soknad.getFaktaMedKey(faktumKey)){
            String barnFaktumId = faktum.getProperties().get("tilknyttetbarn");
            if(barnFaktumId != null && barneFaktum.getFaktumId().equals(Long.parseLong(barnFaktumId))) {
                return options.fn(faktum);
            }
        }
        return options.inverse(this);
    }
}
