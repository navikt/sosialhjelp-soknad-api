package no.nav.sbl.dialogarena.soknad.pages;

import no.nav.sbl.dialogarena.soknad.pages.opensoknad.OpenSoknadPage;
import no.nav.sbl.dialogarena.soknad.pages.startsoknad.StartSoknadPage;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;


public class HomePage extends WebPage {

    @Inject
    private SoknadService soknadService;

    private final WebMarkupContainer body;

    private List<String> soknader = Arrays.asList("Tullesøknad");
    private List<String> gosysId = Arrays.asList("1");
    private String valgtSoknad = soknader.get(0);

    public HomePage() {

        String aktorId = "1234";

        body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);

        body.add(new Label("aktorId", aktorId));

        add(new ListView<Long>("eksisterendeSoknadListe", hentSoknadIder(aktorId)) {
            @Override
            protected void populateItem(final ListItem<Long> item) {
                Link link = new Link("eksisterendeSoknadLink") {
                    @Override
                    public void onClick() {
                        setResponsePage(OpenSoknadPage.class, new PageParameters().set("soknadId", item.getModelObject()));
                    }
                };
                link.add(new Label("eksisterendeSoknadText", item.getModel()));
                item.add(link);
            }
        });

        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                String id = gosysId.get(soknader.indexOf(valgtSoknad));
                setResponsePage(StartSoknadPage.class, new PageParameters().set("navSoknadId", id));

            }
        };
        form.add(new Label("bruker", aktorId));
        DropDownChoice soknadList = new DropDownChoice("soknad", new PropertyModel(this, "valgtSoknad"), soknader);
        form.add(soknadList);

        body.add(form);
    }

    public final WebMarkupContainer getBody() {
        return body;
    }

    private List<Long> hentSoknadIder(String aktorId) {
        return soknadService.hentMineSoknader(aktorId);
    }
}
