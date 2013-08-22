package no.nav.sbl.dialogarena.dokumentinnsending.resource;

import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning.ForhandsvisningBilde;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse som genererer et preview av et dokument fra en url.
 */
public class DokumentForhandsvisningResource extends DynamicImageResource {

    private static final byte[] GENERIC_IMAGE = lastGeneriskBilde();
    private static final Map<String, java.awt.Dimension> SIZES = new HashMap<>();

    {
        SIZES.put("s", ThumbnailConverter.SMALL);
        SIZES.put("l", ThumbnailConverter.LARGE);
    }

    @Inject
    protected ThumbnailConverter converter;

    public DokumentForhandsvisningResource(ThumbnailConverter converter) {
        this.converter = converter;
    }


    @Override
    protected byte[] getImageData(Attributes attributes) {
        StringValue dokumentId = attributes.getParameters().get("dokumentId");
        StringValue side = attributes.getParameters().get("side");
        StringValue size = attributes.getParameters().get("size");
        if (!dokumentId.isEmpty() && SIZES.containsKey(size.toString().toLowerCase())) {
            long id = dokumentId.toLong();
            Dimension dimension = SIZES.get(size.toString().toLowerCase());
            try {
                return converter.hentForhaandsvisning(id, dimension, side.isEmpty() ? 0 : side.toInt());
            } catch (Exception ignore) {
                return GENERIC_IMAGE;
            }
        }
        return new byte[0];
    }

    private static byte[] lastGeneriskBilde() {
        InputStream inputStream = ForhandsvisningBilde.class.getResourceAsStream("/na.gif");
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException ignore) {
            return new byte[0];
        }
    }
}
