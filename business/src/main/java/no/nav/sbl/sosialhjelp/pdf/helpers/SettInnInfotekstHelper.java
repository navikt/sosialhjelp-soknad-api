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

import static no.nav.sbl.dialogarena.common.Spraak.NORSK_BOKMAAL;

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
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER);
        String bundleName = konfigurasjon.getBundleName();

        String infotekst = getInfotekst(key, options, konfigurasjon, bundleName);
        
        if (infotekst == null) {
            return "";
        }
        
        String infotekstTittel = this.cmsTekst.getCmsTekst("infotekst.oppsummering.tittel", options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, NORSK_BOKMAAL);
        
        return createHtmlLayout(infotekst, infotekstTittel);
    }

    private String getInfotekst(String key, Options options, KravdialogInformasjon konfigurasjon,
            String bundleName) {
        String infotekst = this.cmsTekst.getCmsTekst(key, options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, NORSK_BOKMAAL);

        String nyInfotekst = UrlUtils.endreHyperLenkerTilTekst(infotekst);

        if (infotekst != null && !infotekst.equals(nyInfotekst)) {
            infotekst = nyInfotekst;
        }
        return infotekst;
    }
    
    private String createHtmlLayout(String infotekst, String infoTekstTittel) {
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