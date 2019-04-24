package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.common.Spraak;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class SettInnHjelpetekstHelper extends RegistryAwareHelper<String> implements TextWithTitle {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "settInnHjelpetekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Returner html kode for å vise hjelpetekst";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER);
        String bundleName = konfigurasjon.getBundleName();

        String hjelpetekst = TextWithTitle.getText(key, options, konfigurasjon, bundleName, cmsTekst);
        
        if (hjelpetekst == null) {
            return "";
        }
        
        String hjelpetekstTittel = this.cmsTekst.getCmsTekst("hjelpetekst.oppsummering.tittel", options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, Spraak.NORSK_BOKMAAL);
        
        return TextWithTitle.createHtmlLayout(hjelpetekst, hjelpetekstTittel);
    }
}
