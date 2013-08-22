package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter.UploadFormPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import javax.inject.Inject;

public class StartOpplasting extends Panel {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public static final String OPPLASTING_STARTET = "OpplastingStartet";
    private static final long serialVersionUID = 1L;

    public StartOpplasting(String id) {
        super(id);

        add(new Label("tittel", cmsContentRetriever.hentTekst("opplasting.slikGjorDu")));
    }

    protected UploadFormPanel getUploadForm(String id, String fakeUploadClass) {
        return new UploadFormPanel(id, new ResourceModel("opplasting.startOpplasting"), fakeUploadClass, OPPLASTING_STARTET) {
            @Override
            protected void performActionOnSubmit(AjaxRequestTarget target) {
                target.appendJavaScript("trackEventGA('Opplasting', 'Startet opplasting');");
                super.performActionOnSubmit(target);
            }
        };
    }
}
