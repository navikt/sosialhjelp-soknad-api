package no.nav.sbl.dialogarena.service.helpers.tilleggsstonader;

import com.github.jknack.handlebars.*;
import no.nav.sbl.dialogarena.service.helpers.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.*;
import org.springframework.stereotype.*;

import java.io.*;

import static no.nav.sbl.dialogarena.service.HandleBarKjoerer.finnFaktum;
import static no.nav.sbl.dialogarena.service.HandleBarKjoerer.finnWebSoknad;

@Component
public class ForFaktumTilknyttetBarn extends RegistryAwareHelper<String>{

    public static final String NAVN = "forFaktumTilknyttetBarn";
    public static final ForFaktumTilknyttetBarn INSTANS = new ForFaktumTilknyttetBarn();

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public Helper<String> getHelper() {
        return INSTANS;
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
