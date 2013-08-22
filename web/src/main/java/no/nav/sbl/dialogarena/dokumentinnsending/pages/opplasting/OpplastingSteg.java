package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.modig.content.CmsContentRetriever;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import javax.inject.Inject;

import static no.nav.modig.wicket.conditional.ConditionalUtils.hasCssClassIf;

public class OpplastingSteg extends Panel {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;
    public OpplastingSteg(String id, int steg, String resourceKey) {
        super(id);

        add(new Label("steg", new Model(steg)));
        add(new Label("tittel", new ResourceModel(resourceKey + ".tittel")));
        add(new Label("beskrivelse", new ResourceModel(resourceKey + ".beskrivelse"))
                .setEscapeModelStrings(false));
        add(new WebMarkupContainer("button"));
    }

    public void setButton(Component button, String buttonClass) {
        button.add(hasCssClassIf(buttonClass, new Model(true)));
        replace(button);
    }

    public String getButtonId() {
        return "button";
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(OnLoadHeaderItem.forScript(String.format("setDivHeightToMatchSiblings('.opplasting-beskrivelse');")));
        super.renderHead(response);
    }
}
