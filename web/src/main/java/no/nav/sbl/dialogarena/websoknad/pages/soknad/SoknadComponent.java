package no.nav.sbl.dialogarena.websoknad.pages.soknad;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class SoknadComponent extends WebComponent {
    private final String soknadType;
    private static List<String> files;


    public SoknadComponent(String id, WebSoknad soknadType) {
        super(id);
        initLegalFilenames();
        this.soknadType = soknadType.getGosysId();
    }

    private void initLegalFilenames() {
        if (files == null) {
            try {
                URL htmls = WebApplication.get().getServletContext().getResource("/html");
                File folder = new File(htmls.toURI());
                List<String> files = new ArrayList<>();
                for (File file : folder.listFiles()) {
                    files.add(file.getName());
                }
                SoknadComponent.files = files;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

        String file = String.format("%s.html", soknadType);
        if (files.contains(file)) {
            InputStream content = WebApplication.get().getServletContext().getResourceAsStream(String.format("/html/%s", file));
            try {
                replaceComponentTagBody(markupStream, openTag, StreamUtils.copyToString(content, Charset.forName("UTF-8")));
            } catch (IOException e) {
                throw new ApplicationException("feilet under lasting av markup", e);
            }
        } else {
            throw new ApplicationException("Fant ikke tempate " + file);
        }
    }
}
