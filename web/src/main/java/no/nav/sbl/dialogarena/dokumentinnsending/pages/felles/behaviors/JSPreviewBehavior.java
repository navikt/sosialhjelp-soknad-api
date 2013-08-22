package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning.ForhandsvisningModel;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning.ForhandsvisningModel.DOKUMENT_TIL_FORHANDSVISNING_MODEL;

public class JSPreviewBehavior extends JsonBehavior {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    @Inject
    private SoknadService soknadService;

    private IModel<Dokument> dokument;

    public JSPreviewBehavior(IModel<Dokument> dokument) {
        this.dokument = dokument;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        ForhandsvisningModel forhandsvisningModel = DOKUMENT_TIL_FORHANDSVISNING_MODEL.transform(soknadService.hentDokumentInnhold(dokument.getObject()));

        JSONObject json = new JSONObject();
        setJsonObject(json, "componentId", component.getMarkupId());
        setJsonObject(json, "dokumentId", dokument.getObject().getDokumentId());
        setJsonObject(json, "dokumentNavn", forhandsvisningModel.filename);
        setJsonObject(json, "antallSider", forhandsvisningModel.getAntallSider());
        setJsonObject(json, "side", getApplicationProperty("side"));
        setJsonObject(json, "av", getApplicationProperty("av"));

        String js = String.format("addPreviewTooltip(%s);", json.toString());
        response.render(OnLoadHeaderItem.forScript(js));
        super.renderHead(component, response);
    }

    private String getApplicationProperty(String key){
        return cmsContentRetriever.hentTekst(key);
    }
}
