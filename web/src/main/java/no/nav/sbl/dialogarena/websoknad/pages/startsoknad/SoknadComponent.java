package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.Charset.forName;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StreamUtils.copyToString;


/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class SoknadComponent extends WebComponent {

    private static final Logger LOGGER = getLogger(SoknadComponent.class);
    private final String soknadType;
    private static List<String> files;

    public SoknadComponent(String id, String soknadType) {
        super(id);
        initLegalFilenames();
        this.soknadType = soknadType;
    }

    private void initLegalFilenames() {
        if (files == null) {
            try {
                String htmls = WebApplication.get().getServletContext().getRealPath("/html");
                File folder = new File(htmls);
                List<String> filer = new ArrayList<>();
                for (File file : folder.listFiles()) {
                    filer.add(file.getName());
                }
                SoknadComponent.files = filer;
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String file = format("%s.html", soknadType);
        if (files.contains(file)) {
            try (InputStream content = WebApplication.get().getServletContext().getResourceAsStream(format("/html/%s", file))) {
                replaceComponentTagBody(markupStream, openTag, copyToString(content, forName("UTF-8")));
            } catch (IOException e) {
                throw new ApplicationException("feilet under lasting av markup", e);
            }
        } else {
            throw new ApplicationException("Fant ikke template " + file);
        }
    }

}
