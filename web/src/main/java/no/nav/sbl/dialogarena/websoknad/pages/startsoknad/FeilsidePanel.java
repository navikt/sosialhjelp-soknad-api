package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.modig.content.CmsContentRetriever;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Klasse som laster inn en html snutt fra en tempate inn i siden.
 */
public class FeilsidePanel extends Panel {
    @Inject
    private CmsContentRetriever cmsContentRetriever;

    private static final Logger logger = getLogger(FeilsidePanel.class);
    public FeilsidePanel(String id) {
        super(id);
    }
}
