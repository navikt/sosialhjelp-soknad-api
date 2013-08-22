package no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.JournalfoerendeEnhet;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AdresseValg extends Panel {
	
	public static final String VALG_1 = "valg1";
	public static final String VALG_2 = "valg2";
	public static final String VALG_3 = "valg3";

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public AdresseValg(String id, RadioGroup radioGroup, List<String> resources) {
        super(id);

        ArrayList<String> newList = new ArrayList<>(resources);
        Radio radioButton = new Radio("radioButton", new Model<>(id), radioGroup) {
            @Override
            public String getValue() {
                return getModelObject().toString();
            }
        };
        add(radioButton);
        WebMarkupContainer label = new WebMarkupContainer("label");
        label.add(new AttributeModifier("for", radioButton.getMarkupId()));
        add(label);
        label.add(new Label("hovedpunkt", cmsContentRetriever.hentTekst(newList.remove(0))));

        ListView<String> punkt = new ListView<String>("punkt", newList) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String resource = item.getModelObject();

                char index = (char) (item.getParent().size() + 64);
                item.add(new Label("index", index));
                item.add(new Label("punktLabel", cmsContentRetriever.hentTekst(resource)));
            }
        };
        punkt.setVisible(!resources.isEmpty());
        label.add(punkt);
    }


	public static String journalFoerendeEnhet(String valg) {
		if (VALG_3.equalsIgnoreCase(valg)) {
			return JournalfoerendeEnhet.NAV_INTERNASJONAL.kode();
		} else {
			return "";
		}
	}
}
