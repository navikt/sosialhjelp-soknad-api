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
public class SettInnKnappTilgjengeligTekstHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Override
    public String getNavn() {
        return "settInnKnappTilgjengeligTekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Returner html kode for å vise knapp tilgjengelig";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        final KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER);
        final String bundleName = konfigurasjon.getBundleName();

        String knapptekst = getKnapptekst(key, options, konfigurasjon, bundleName);

        if (knapptekst == null) {
            return "";
        }

        final String knapptekstTittel = "Knapp tilgjengelig:";

        return createHtmlLayout(knapptekst, knapptekstTittel);
    }

    private String getKnapptekst(String key, Options options, final KravdialogInformasjon konfigurasjon,
                                final String bundleName) {
        String knapptekst = this.cmsTekst.getCmsTekst(key, options.params, konfigurasjon.getSoknadTypePrefix(), bundleName, SPRAK);

        String nyKnapptekst = UrlUtils.endreHyperLenkerTilTekst(knapptekst);

        if (knapptekst != null && !knapptekst.equals(nyKnapptekst)) {
            knapptekst = nyKnapptekst;
        }
        return knapptekst;
    }

    private String createHtmlLayout(String knapptekst, final String knapptekstTittel) {
        return "<ul class=\"svar-liste\">\r\n" +
                "    <li>\r\n" +
                "        <h4>" + knapptekstTittel + "</h4>\r\n" +
                "    </li>\r\n" +
                "    <li>\r\n" +
                "        <span>" + knapptekst + "</span>\r\n" +
                "    </li>\r\n" +
                "</ul>";
    }
}