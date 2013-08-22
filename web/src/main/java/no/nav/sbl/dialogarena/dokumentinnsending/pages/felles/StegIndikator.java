package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import no.nav.modig.content.CmsContentRetriever;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.wicket.conditional.ConditionalUtils.hasCssClassIf;

public class StegIndikator extends Panel {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public static final List<String> STANDARD_STEG = Arrays.asList("stegindikator.steg1", "stegindikator.steg2", "stegindikator.steg3");

    public StegIndikator(String id) {
        super(id);

        ListView<String> stegListe = new ListView<String>("steg", STANDARD_STEG) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String resourceString = item.getModelObject();
                int aktivtSteg = ((BaseViewModel) getPage().getDefaultModelObject()).aktivtSteg;

                IModel cssClassModel = new Model<>(item.getParent().size() == aktivtSteg);
                item.add(hasCssClassIf("aktiv", cssClassModel));

                item.add(new Label("stegTekst", cmsContentRetriever.hentTekst(resourceString)));
            }
        };
        add(stegListe);
    }
}
