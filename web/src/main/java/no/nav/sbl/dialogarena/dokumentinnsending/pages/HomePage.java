package no.nav.sbl.dialogarena.dokumentinnsending.pages;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.StartSoknadPage;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;

import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HomePage extends WebPage {

	@Inject
    private WebSoknadService websoknadService;
    
    private List<String> websoknader = Arrays.asList("Tullesøknad");
    private List<String> gosysId = Arrays.asList("1");
    private String valgtWebSoknad = websoknader.get(0);
    
    public HomePage() {
        super(new PageParameters());

        TransparentWebMarkupContainer body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);

        LoadableDetachableModel<String> aktorId = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return SubjectHandler.getSubjectHandler().getUid();
            }
        };
        add(new Label("aktorId", aktorId));
        //WEBSOKNAD
        add(new ListView<Long>("eksisterendeWebSoknadListe", hentSoknadIder("HARDCODED_VALUE_OVERRIDDEN_IN_HENVENDELSE")) {
            @Override
            protected void populateItem(final ListItem<Long> item) {
                Link link = new Link("eksisterendeWebSoknadLink") {
                    @Override
                    public void onClick() {
                    }
                };
                link.add(new Label("eksisterendeWebSoknadText", item.getModelObject()));
                item.add(link);
            }
        });
                
        Form websoknadForm = new Form("websoknadForm") {
            @Override
            protected void onSubmit() {
                String id = gosysId.get(websoknader.indexOf(valgtWebSoknad));
                setResponsePage(StartSoknadPage.class, new PageParameters().set("navSoknadId", id));

            }
        };
        add(websoknadForm);
        websoknadForm.add(new Label("bruker", aktorId));
        DropDownChoice websoknadList = new DropDownChoice("websoknad", new PropertyModel(this, "valgtWebSoknad"), websoknader);
        websoknadForm.add(websoknadList);
    }
    
    private List<Long> hentSoknadIder(String aktorId) {
        return websoknadService.hentMineSoknader(aktorId);
    }

    private List<String> getKodeverksIder(List<? extends Dokument> navVedlegg, List<? extends Dokument> eksterntVedlegg, List<? extends Dokument> annetVedlegg) {
        List<String> list = new ArrayList<>();
        for (Dokument dok : navVedlegg) {
            list.add(dok.getKodeverkId());
        }

        for (Dokument dok : eksterntVedlegg) {
            list.add(dok.getKodeverkId());
        }

        for (Dokument dok : annetVedlegg) {
            list.add(dok.getKodeverkId());
        }
        return list;
    }

}