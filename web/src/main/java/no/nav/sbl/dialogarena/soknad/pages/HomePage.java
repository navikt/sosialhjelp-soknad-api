package no.nav.sbl.dialogarena.soknad.pages;

import no.nav.sbl.dialogarena.soknad.pages.soknad.SoknadPage;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;


public class HomePage extends WebPage {

    private final WebMarkupContainer body;



    public HomePage() {
        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);

        body.add(new Link("xml") {
            @Override
            public void onClick() {
                setResponsePage(SoknadPage.class, new PageParameters().set("soknadId", 1));
            }
        });

        body.add(new Link("json") {
            @Override
            public void onClick() {
                setResponsePage(SoknadPage.class, new PageParameters().set("soknadId", 2));
            }
        });
    }

    public WebMarkupContainer getBody() {
        return body;
    }

//    private XmlSoknad getXmlParser() throws URISyntaxException, IOException {
//        return new XmlSoknad(soknadService.hentSoknad(1L));
//    }
//
//    private JsonSoknad getJsonParser() throws URISyntaxException, IOException {
//        return new JsonSoknad(soknadService.hentSoknad(2L));
//    }
}
