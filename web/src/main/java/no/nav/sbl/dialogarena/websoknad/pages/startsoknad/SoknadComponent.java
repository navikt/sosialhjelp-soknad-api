package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class SoknadComponent extends WebComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoknadComponent.class);
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
                List<String> files = new ArrayList<>();
                for (File file : folder.listFiles()) {
                    files.add(file.getName());
                }
                SoknadComponent.files = files;
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

        String file = String.format("%s.html", soknadType);
        if (files.contains(file)) {
            
            try (InputStream content = WebApplication.get().getServletContext().getResourceAsStream(String.format("/html/%s", file))){
            	
                replaceComponentTagBody(markupStream, openTag, StreamUtils.copyToString(content, Charset.forName("UTF-8")));
            } catch (IOException e) {
                throw new ApplicationException("feilet under lasting av markup", e);
            }
        } else {
            throw new ApplicationException("Fant ikke template " + file);
        }
    }
}
