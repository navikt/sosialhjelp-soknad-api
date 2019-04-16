package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import no.nav.sbl.sosialhjelp.pdf.UrlUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class SettInnHjelpetekstHelper extends RegistryAwareHelper<String> {

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

        String hjelpetekst = getHjelpetekst(key, options, konfigurasjon, bundleName);
        
        if (hjelpetekst == null) {
            return "";
        }
        
        String hjelpetekstTittel = this.cmsTekst.getCmsTekst("hjelpetekst.oppsummering.tittel", options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, SPRAK);
        
        return createHtmlLayout(hjelpetekst, hjelpetekstTittel);
    }

    private String getHjelpetekst(String key, Options options, KravdialogInformasjon konfigurasjon, String bundleName) {
        String hjelpeTekst = this.cmsTekst.getCmsTekst(key, options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, SPRAK);

        String nyHjelpeTekst = UrlUtils.endreHyperLenkerTilTekst(hjelpeTekst);

        if (hjelpeTekst != null && !hjelpeTekst.equals(nyHjelpeTekst)) {
            hjelpeTekst = nyHjelpeTekst;
        }
        return hjelpeTekst;
    }
    
    private String createHtmlLayout(String hjelpetekst, String hjelpetekstTittel) {
        return "<ul class=\"svar-liste\">\r\n" + 
                "    <li>\r\n" + 
                "        <h4>" + hjelpetekstTittel + "</h4>\r\n" + 
                "    </li>\r\n" + 
                "    <li>\r\n" + 
                "        <span>" + hjelpetekst + "</span>\r\n" + 
                "    </li>\r\n" + 
                "</ul>";
    }
}
