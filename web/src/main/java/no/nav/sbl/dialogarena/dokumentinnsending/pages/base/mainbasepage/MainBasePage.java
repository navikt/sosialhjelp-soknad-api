package no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage;

import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.BasePage;
import no.nav.sbl.dialogarena.webkomponent.footer.FooterPanel;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.webkomponent.navigasjon.NavigasjonPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

public class MainBasePage extends BasePage {

    @Inject
    @Named("navigasjonslink")
    private String navigasjonsLink;

    @Inject
    @Named("footerLinks")
    private Map<String, String> footerLinks;

    public MainBasePage(PageParameters parameters) {
        super(parameters);

        add(new Label("tittel"));
        add(new Label("tabTittel"));
        add(new InnstillingerPanel("innstillinger", getInnloggetIsTrueModel(), cmsContentRetriever));
        add(new NavigasjonPanel("navigasjon", navigasjonsLink));
        add(new FooterPanel("footer", footerLinks, getInnloggetIsTrueModel(), FALSE, cmsContentRetriever));
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
