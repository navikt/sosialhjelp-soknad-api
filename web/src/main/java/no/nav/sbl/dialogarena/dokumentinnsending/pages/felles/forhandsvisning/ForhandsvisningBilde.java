package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning;

import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;


public class ForhandsvisningBilde extends Panel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForhandsvisningBilde.class);

    private static final Image GENERIC_IMAGE = lastGeneriskBilde();

    @Inject
    protected ThumbnailConverter converter;

    public Boolean loading = Boolean.TRUE;
    private Dimension dimension = ThumbnailConverter.LARGE;

    public ForhandsvisningBilde(String id, IModel<ForhandsvisningModel> dokument, int side, Dimension dimension) {
        super(id, dokument);
        this.dimension = dimension;
        add(new AttributeAppender("class", "ferdig"));
        setOutputMarkupId(true);
        add(renderImageFromPage(side));
    }


    private Image renderImageFromPage(Integer side) {
        ForhandsvisningModel forhandsvisningModel = ((IModel<ForhandsvisningModel>) getDefaultModel()).getObject();
        try {
            byte[] bytes = converter.hentForhaandsvisning(forhandsvisningModel.id, dimension, side, forhandsvisningModel.bilde);
            return renderPreview(Model.of(bytes));
        } catch (Exception ex) {
            return GENERIC_IMAGE;
        }
    }

    private Image renderPreview(IModel<byte[]> thumb) {
        return TIL_BILDE.transform(thumb);
    }

    private static final Transformer<IModel<byte[]>, Image> TIL_BILDE = new Transformer<IModel<byte[]>, Image>() {
        @Override
        public Image transform(final IModel<byte[]> bytes) {
            Image image = new NonCachingImage("bilde", new ByteArrayResource("image/png", bytes.getObject()));
            image.setOutputMarkupId(true);
            return image;
        }
    };

    private static Image lastGeneriskBilde() {
        InputStream inputStream = ForhandsvisningBilde.class.getResourceAsStream("/na.gif");
        Image image = null;
        try {
            image = new NonCachingImage("bilde", new ByteArrayResource("image/gif", IOUtils.toByteArray(inputStream), "na.gif"));
            image.setOutputMarkupId(true);

        } catch (IOException ignore) {
            LOGGER.info(ignore.getMessage());
        }
        return image;
    }
}