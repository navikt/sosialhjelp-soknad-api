package no.nav.sbl.dialogarena.soknad.pages.basepage;

import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;


public class BasePage extends WebPage {


    private final WebMarkupContainer body;

    public BasePage() {
        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);
        add(new Label("tabTittel"));
    }

    public WebMarkupContainer getBody() {
        return body;
    }
}
