package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.Charset.forName;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StreamUtils.copyToString;


/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class SoknadComponent extends WebComponent {

    private static final Logger LOGGER = getLogger(SoknadComponent.class);
    private static List<String> files;

    public SoknadComponent(String id) {
        super(id);
        initLegalFilenames();
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
                LOGGER.error("Feil ved lasting av filer:" + ex.getMessage() + " med feillogg" + ex.getStackTrace()) ;
            }
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String file = "META-INF/resources/views/bootstrap.html";
        try (InputStream content = this.getClass().getClassLoader().getResourceAsStream(file)) {
            replaceComponentTagBody(markupStream, openTag, copyToString(content, forName("UTF-8")));
        } catch (IllegalArgumentException| IOException e) {
            try {
                File basedir = new File(WebApplication.get().getServletContext().getResource("/").toURI());
                File devDir = new File(basedir, "../../../../frontend/views/built/bootstrapDev.html");
                try(InputStream content = new FileInputStream(devDir)){
                    replaceComponentTagBody(markupStream, openTag, copyToString(content, forName("UTF-8")));
                }
            } catch (IOException |URISyntaxException e2) {
                LOGGER.warn("Problem med ressurslasting " + file, e2);
                //throw new ApplicationException("feilet under lasting av markup", e);
            }
        }
    }
}
