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
        String sprak = "nb_NO";
        final KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER);
        final String bundleName = konfigurasjon.getBundleName();

        String hjelpetekst = getHjelpetekst(key, options, sprak, konfigurasjon, bundleName);
        
        if (hjelpetekst == null) {
            return "";
        }
        
        final String hjelpetekstTittel = this.cmsTekst.getCmsTekst("hjelpetekst.oppsummering.tittel", options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, toLocale(sprak));
        
        return createHtmlLayout(hjelpetekst, hjelpetekstTittel);
    }

    private String getHjelpetekst(String key, Options options, String sprak, final KravdialogInformasjon konfigurasjon,
            final String bundleName) {
        String hjelpeTekst = this.cmsTekst.getCmsTekst(key, options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, toLocale(sprak));

        String nyHjelpeTekst = UrlUtils.endreHyperLenkerTilTekst(hjelpeTekst);

        if (hjelpeTekst != null && !hjelpeTekst.equals(nyHjelpeTekst)) {
            hjelpeTekst = nyHjelpeTekst;
        }
        return hjelpeTekst;
    }
    
    private String createHtmlLayout(String hjelpetekst, final String hjelpetekstTittel) {
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
