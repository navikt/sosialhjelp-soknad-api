package no.nav.sbl.dialogarena.soknad.pages;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknad.convert.json.JsonSoknad;
import no.nav.sbl.dialogarena.soknad.convert.xml.XmlSoknad;
import no.nav.sbl.dialogarena.soknad.pages.soknad.SoknadPage;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;


public class HomePage extends WebPage {

    private final WebMarkupContainer body;

    @Inject
    private SoknadService soknadService;

    public HomePage() {
        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);

        body.add(new Link("xml") {
            @Override
            public void onClick() {
                try {
                    setResponsePage(new SoknadPage(getXmlParser("testsoknad.xml")));
                } catch (URISyntaxException | IOException e) {
                    throw new ApplicationException("Kunne ikke bygge opp søknaden", e);
                }
            }
        });

        body.add(new Link("json") {
            @Override
            public void onClick() {
                try {
                    setResponsePage(new SoknadPage(getJsonParser("testsoknad.json")));
                } catch (URISyntaxException | IOException e) {
                    throw new ApplicationException("Kunne ikke bygge opp søknaden", e);
                }
            }
        });
    }

    public WebMarkupContainer getBody() {
        return body;
    }

    private XmlSoknad getXmlParser(String filename) throws URISyntaxException, IOException {
//        String path = "/no/nav/sbl/dialogarena/soknad/xml/";
//        File file = new File(getClass().getResource(path + filename).toURI());
//        String xmlString = file.readString();
        return new XmlSoknad(soknadService.hentSoknad(1L));
    }

    private JsonSoknad getJsonParser(String filename) throws URISyntaxException, IOException {
//        String path = "/no/nav/sbl/dialogarena/soknad/json/";
//        File file = new File(getClass().getResource(path + filename).toURI());
//        String jsonString = file.readString();
        return new JsonSoknad(soknadService.hentSoknad(2L));
    }
}
