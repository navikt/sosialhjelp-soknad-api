package no.nav.sbl.dialogarena.dokumentinnsending.pages.base;

import no.nav.modig.content.CmsContentRetriever;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class BasePage extends WebPage implements IHeaderContributor {

    private final WebMarkupContainer body;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public BasePage(PageParameters parameters) {
        super(parameters);
        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);
    }

    public WebMarkupContainer getBody() {
        return body;
    }
}