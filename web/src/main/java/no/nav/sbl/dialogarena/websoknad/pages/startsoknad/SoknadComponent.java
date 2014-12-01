package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.Charset.forName;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StreamUtils.copyToString;


/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class SoknadComponent extends WebComponent {

    private static final Logger logger = getLogger(SoknadComponent.class);
    private SkjemaBootstrapFile skjemaBootstrapPostfix;


    public SoknadComponent(String id, SkjemaBootstrapFile skjemaBootstrapPostfix) {
        super(id);
        this.skjemaBootstrapPostfix = skjemaBootstrapPostfix;
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String file = "META-INF/resources/views/built/bootstrap";
        if (RuntimeConfigurationType.DEVELOPMENT.equals(getApplication().getConfigurationType())) {
            file = "META-INF/resources/views/built/bootstrapDev";
        }

        file += skjemaBootstrapPostfix + ".html";

        try (InputStream content = this.getClass().getClassLoader().getResourceAsStream(file)) {
            replaceComponentTagBody(markupStream, openTag, copyToString(content, forName("UTF-8")));
        } catch (IllegalArgumentException| IOException e) {
            logger.warn("Problem med ressurslasting " + file, e);
        }
    }
}
