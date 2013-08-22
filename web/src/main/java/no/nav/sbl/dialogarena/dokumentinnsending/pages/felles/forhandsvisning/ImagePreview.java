package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning;

import no.nav.modig.wicket.events.NamedEventPayload;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.OpplastetFil;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class ImagePreview extends Panel {

    public static final String SLETT_OPPLASTET_FIL = "SlettOpplastetFil";

    public ImagePreview(String id, final OpplastetFil upload) {
        super(id);
        ForhandsvisningModel forhandsvisningModel = new ForhandsvisningModel();
        forhandsvisningModel.id = Arrays.toString(upload.md5);
        forhandsvisningModel.filename = upload.name;
        forhandsvisningModel.bilde = upload.innhold;

        IModel<ForhandsvisningModel> model = Model.of(forhandsvisningModel);

        setDefaultModel(model);
        Label navn = new Label("filename", new PropertyModel<>(model, "filename")) {
            @Override
            public void renderHead(IHeaderResponse response) {
                String js = String.format("truncateString($('#%s'))", this.getMarkupId());
                response.render(OnLoadHeaderItem.forScript(js));
                super.renderHead(response);
            }
        };
        add(navn);
        add(new DokumentForhandsvisning("forhandsvis", model));
        AjaxLink slett = new AjaxLink("slett") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                send(getPage(), Broadcast.DEPTH, new NamedEventPayload(SLETT_OPPLASTET_FIL, upload));
            }
        };
        add(slett);
    }
}
