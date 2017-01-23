package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static org.apache.commons.lang3.LocaleUtils.toLocale;

@Component
public class HentTekstMedFaktumParameterHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "hentTekstMedFaktumParameter";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms for en gitt key, med verdien til et faktum som parameter. Faktumet hentes basert på key";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum faktum = soknad.getFaktumMedKey(options.param(0).toString());
        String prefix = soknad.getSoknadPrefix();
        final String bundleName = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getBundleName();
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();

        String tekst = this.cmsTekst.getCmsTekst(key, new Object[]{faktum.getValue()}, prefix, bundleName, toLocale(sprak));
        return tekst != null ? tekst : "";
    }
}
