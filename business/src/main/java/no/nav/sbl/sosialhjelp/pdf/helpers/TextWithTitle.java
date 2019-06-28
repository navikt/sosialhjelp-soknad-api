package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import no.nav.sbl.sosialhjelp.pdf.UrlUtils;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

public interface TextWithTitle {
    static String getText(String key, Options options, String soknadTypePrefix, String bundleName, CmsTekst cmsTekst) {
        String text = cmsTekst.getCmsTekst(key, options.params, soknadTypePrefix, bundleName, SPRAK);

        String newText = UrlUtils.endreHyperLenkerTilTekst(text);

        if (text != null && !text.equals(newText)) {
            text = newText;
        }
        return text;
    }

    static String createHtmlLayout(String text, final String title) {
        return "<ul class=\"svar-liste\">\r\n" +
                "    <li>\r\n" +
                "        <h4>" + title + "</h4>\r\n" +
                "    </li>\r\n" +
                "    <li>\r\n" +
                "        <span>" + text + "</span>\r\n" +
                "    </li>\r\n" +
                "</ul>";
    }
}
