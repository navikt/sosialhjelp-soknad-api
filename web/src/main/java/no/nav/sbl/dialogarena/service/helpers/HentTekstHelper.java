package no.nav.sbl.dialogarena.service.helpers;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Options;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.utils.UrlUtils;

@Component
public class HentTekstHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "hentTekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        String sprak = "nb_NO";
        final KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER);
        final String bundleName = konfigurasjon.getBundleName();

        String tekst = this.cmsTekst.getCmsTekst(key, options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, toLocale(sprak));

        String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

        if (tekst != null && !tekst.equals(nyTekst)) {
            tekst = nyTekst;
        }
        return tekst != null ? tekst : "";
    }
}
