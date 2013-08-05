package no.nav.sbl.dialogarena.soknad.pages.basepage;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import no.nav.sbl.dialogarena.webkomponent.footer.FooterPanel;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.webkomponent.navigasjon.NavigasjonPanel;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


public class BasePage extends WebPage {

    @Inject
    private SoknadService soknadService;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    @Inject
    @Named("navigasjonslink")
    private String navigasjonsLink;

    @Inject
    @Named("footerLinks")
    private Map<String, String> footerLinks;

    private final WebMarkupContainer body;
    private final Form form;

    public BasePage() {
        super();
        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);

        form = new Form("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                Long soknadId = ((BaseViewModel) getPage().getDefaultModelObject()).getSoknadId();
                soknadService.sendSoknad(soknadId);
            }
        };
        body.add(form);

        add(new Label("tabTittel"));
        add(new Label("tittel"));
        add(new InnstillingerPanel("innstillinger", getInnloggetIsTrueModel(), cmsContentRetriever));
        add(new NavigasjonPanel("navigasjon", navigasjonsLink));
        add(new FooterPanel("footer", footerLinks, getInnloggetIsTrueModel(), FALSE, cmsContentRetriever));
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