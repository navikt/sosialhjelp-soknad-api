package no.nav.sbl.dialogarena.websoknad.pages.basepage;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.webkomponent.navigasjon.NavigasjonPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import javax.inject.Named;

public class AvbruttBasePage extends WebPage {

    @Inject
    private CmsContentRetriever cmsContentRetriever;

    @Inject
    @Named("navigasjonslink")
    private String navigasjonsLink;

    public AvbruttBasePage(PageParameters parameters) {
        super(parameters);

        add(
                new InnstillingerPanel("innstillinger", getInnloggetIsTrueModel(), cmsContentRetriever, false),
                new NavigasjonPanel("navigasjon", navigasjonsLink, cmsContentRetriever)
        );

        add(new ExternalLink("dittnav", System.getProperty("dittnav.link.url")));
    }

    private AbstractReadOnlyModel<Boolean> getInnloggetIsTrueModel() {
        return new AbstractReadOnlyModel<Boolean>() {
            @Override
            public Boolean getObject() {
                return true;
            }
        };
    }
}
