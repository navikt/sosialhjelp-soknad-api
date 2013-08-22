package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.PreloadedImage;
import no.nav.sbl.dialogarena.dokumentinnsending.resource.DokumentForhandsvisningResourceReference;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class OpplastetDokument extends GenericPanel<Dokument> {

    @Inject
    private DokumentForhandsvisningResourceReference thumbnailRef;

    public OpplastetDokument(String id, IModel<Dokument> dokument) {
        super(id, dokument);
        setOutputMarkupId(true);
        add(new PreloadedImage("forside", imageUrl()));
        add(new OpplastetDokumentInfo("overlegg", getModel()));
    }

    private IModel<String> imageUrl() {

        return new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                PageParameters params = new PageParameters();
                params.add("size", "l");
                params.add("dokumentId", getModel().getObject().getDokumentId());
                params.add("side", 0);
                CharSequence charSequence = getRequestCycle().urlFor(thumbnailRef, params);
                return charSequence.toString();
            }
        };
    }
}