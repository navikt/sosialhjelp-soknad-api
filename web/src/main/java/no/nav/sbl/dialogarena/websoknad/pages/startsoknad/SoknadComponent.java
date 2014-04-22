package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

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
    private static final String ETTERSENDING_POSTFIX = "Ettersending";
    private Boolean erEttersending;


    public SoknadComponent(String id) {
        this(id, false);
    }

    public SoknadComponent(String id, Boolean erEttersending) {
        super(id);
        this.erEttersending = erEttersending;
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String file = "META-INF/resources/views/built/bootstrap";
        if (RuntimeConfigurationType.DEVELOPMENT.equals(getApplication().getConfigurationType())) {
            file = "META-INF/resources/views/built/bootstrapDev";
        }

        if (erEttersending) {
            file = file + ETTERSENDING_POSTFIX;
        }

        file = file + ".html";

        try (InputStream content = this.getClass().getClassLoader().getResourceAsStream(file)) {
            replaceComponentTagBody(markupStream, openTag, copyToString(content, forName("UTF-8")));
        } catch (IllegalArgumentException| IOException e) {
            logger.warn("Problem med ressurslasting " + file, e);
        }
    }
}
