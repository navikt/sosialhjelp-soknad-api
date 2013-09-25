package no.nav.sbl.dialogarena.websoknad.pages.basepage;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.webkomponent.footer.FooterPanel;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.webkomponent.navigasjon.NavigasjonPanel;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


public class BasePage extends WebPage {

    @Inject
    private CmsContentRetriever cmsContentRetriever;

    @Inject
    @Named("navigasjonslink")
    private String navigasjonsLink;

    @Inject
    @Named("footerLinks")
	private Map<String, String> footerLinks;

    private final WebMarkupContainer body;

    public BasePage(PageParameters parameters) {
        super(parameters);
        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);
        add(new Label("tabTittel"));


        body.add(new Label("tittel"));

        body.add(new InnstillingerPanel("innstillinger", getInnloggetIsTrueModel(), cmsContentRetriever));
        body.add(new NavigasjonPanel("navigasjon", navigasjonsLink));
        body.add(new FooterPanel("footer", footerLinks, getInnloggetIsTrueModel(), FALSE, cmsContentRetriever));
    }

    public final WebMarkupContainer getBody() {
        return body;
    }

    private AbstractReadOnlyModel<Boolean> getInnloggetIsTrueModel() {
        return new AbstractReadOnlyModel<Boolean>() {
            @Override
            public Boolean getObject() {
                return true;
            }
        };
    }

    private static final IModel<Boolean> FALSE = new AbstractReadOnlyModel<Boolean>() {
        @Override
        public Boolean getObject() {
            return false;
        }
    };
}