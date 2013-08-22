package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.UrlResourceReference;

public class PreloadedImage extends Image {

    private static final UrlResourceReference SPINNER = new UrlResourceReference(Url.parse(UrlUtils.rewriteToContextRelative("img/ajaxloader/snurrepipp_rod_48.gif", RequestCycle.get())));

    private final IModel<String> toLoad;

    public PreloadedImage(String id, IModel<String> toLoad) {
        super(id, SPINNER);
        this.toLoad = toLoad;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        String js = String.format("preloadImage('%s', '%s');", getMarkupId(), toLoad.getObject());
        response.render(OnLoadHeaderItem.forScript(js));
        super.renderHead(response);
    }
}
