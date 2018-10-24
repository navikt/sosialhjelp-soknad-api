package no.nav.sbl.dialogarena.service.helpers;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Options;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.Soknadsmottaker;

@Component
public class HentNavEnhetNavnHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentNavEnhetNavn";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Henter navnet på NAV enheten som bruker har valgt";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);

        if (soknad.erEttersending()) {
            return soknad.getValueForFaktum("ettersendelse.sendestil");
        } else {
            final Soknadsmottaker soknadsmottaker = KommuneTilNavEnhetMapper.getSoknadsmottaker(soknad);
            return soknadsmottaker.getSammensattNavn();
        }

    }
}
