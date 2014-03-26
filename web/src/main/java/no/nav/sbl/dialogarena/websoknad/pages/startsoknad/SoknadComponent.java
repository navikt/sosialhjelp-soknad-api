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

import static java.nio.charset.Charset.forName;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StreamUtils.copyToString;


/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class SoknadComponent extends WebComponent {

    private static final Logger logger = getLogger(SoknadComponent.class);

    public SoknadComponent(String id) {
        super(id);
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
                logger.warn("Problem med ressurslasting " + file, e2);
                //throw new ApplicationException("feilet under lasting av markup", e);
            }
        }
    }
}
