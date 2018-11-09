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
public class SettInnInfotekstHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "settInnInfotekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Returner html kode for å vise infotekst";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        String sprak = "nb_NO";
        final KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER);
        final String bundleName = konfigurasjon.getBundleName();

        String infotekst = getInfotekst(key, options, sprak, konfigurasjon, bundleName);
        
        if (infotekst == null) {
            return "";
        }
        
        final String infotekstTittel = this.cmsTekst.getCmsTekst("infotekst.oppsummering.tittel", options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, toLocale(sprak));
        
        return createHtmlLayout(infotekst, infotekstTittel);
    }

    private String getInfotekst(String key, Options options, String sprak, final KravdialogInformasjon konfigurasjon,
            final String bundleName) {
        String infotekst = this.cmsTekst.getCmsTekst(key, options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, toLocale(sprak));

        String nyInfotekst = UrlUtils.endreHyperLenkerTilTekst(infotekst);

        if (infotekst != null && !infotekst.equals(nyInfotekst)) {
            infotekst = nyInfotekst;
        }
        return infotekst;
    }
    
    private String createHtmlLayout(String infotekst, final String infoTekstTittel) {
        return "<ul class=\"svar-liste\">\r\n" + 
                "    <li>\r\n" + 
                "        <h4>" + infoTekstTittel + "</h4>\r\n" + 
                "    </li>\r\n" + 
                "    <li>\r\n" + 
                "        <span>" + infotekst + "</span>\r\n" + 
                "    </li>\r\n" + 
                "</ul>";
    }
}