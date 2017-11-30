package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class VisCheckboxHelper extends RegistryAwareHelper<String> {
    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public static final String NAVN = "visCheckbox";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "hvis value er \"true\" eller key.false-teksten finnes";
    }

    @Override
    public CharSequence apply(String value, Options options) throws IOException {
        String key = options.param(0);
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        String soknadPrefix = soknad.getSoknadPrefix();
        final String bundleName = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();

        String falseTekst = this.cmsTekst.getCmsTekst(key + ".false", new Object[0], soknadPrefix, bundleName, toLocale(sprak));
        if (falseTekst != null || "true".equals(value)) {
            return options.fn();
        } else {
            return options.inverse();
        }
    }
}
