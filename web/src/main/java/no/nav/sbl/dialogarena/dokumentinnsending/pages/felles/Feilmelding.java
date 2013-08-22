package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;

public class Feilmelding extends FencedFeedbackPanel {
    private static final long serialVersionUID = 1L;

    public Feilmelding(String id) {
        this(id, null);
    }

    public Feilmelding(String id, String tittel) {
        super(id);

        setOutputMarkupPlaceholderTag(true);
        Label label = new Label("tittel", tittel);
        label.setVisible(tittel != null);
        add(label);
    }

    @Override
    protected void onConfigure()
    {
        super.onConfigure();
        setVisible(anyMessage());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        String js = String.format("popToErrorMessage('%s');", getMarkupId());

        response.render(OnLoadHeaderItem.forScript(js));
        super.renderHead(response);
    }
}
