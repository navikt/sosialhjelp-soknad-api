package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning;


import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.UrlResourceReference;

import java.awt.Dimension;

public class LazyForhandsvisning extends AjaxLazyLoadPanel {

    private Dimension dimension = ThumbnailConverter.LARGE;
    private final int currentPage;

    public LazyForhandsvisning(String id, final IModel<ForhandsvisningModel> dokument, int side, Dimension dimension) {
        super(id, dokument);
        this.currentPage = side;
        this.dimension = dimension;
    }

    @Override
    public Component getLazyLoadComponent(String markupId) {
        return new ForhandsvisningBilde(markupId, (IModel<ForhandsvisningModel>) getDefaultModel(), currentPage, dimension);
    }

    @Override
    public Component getLoadingComponent(String markupId) {

        IRequestHandler handler = new ResourceReferenceRequestHandler(
                new UrlResourceReference(Url.parse(UrlUtils.rewriteToContextRelative("img/ajaxloader/hvit/loader_hvit_64.gif", RequestCycle.get()))));
        return new Label(markupId, "<img alt=\"Loading...\" src=\"" +
                RequestCycle.get().urlFor(handler) + "\"/>").setEscapeModelStrings(false);

    }
}
