package no.nav.sbl.dialogarena.dokumentinnsending.convert;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import org.springframework.cache.annotation.Cacheable;

import java.awt.Dimension;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: w139306
 * Date: 18.07.13
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public interface ThumbnailConverter {
    // Hvor lenge en skal vente på svar fra generering av miniatyr av bildet. Tid i millis.
    Dimension SMALL = new Dimension(140, 196);
    Dimension LARGE = new Dimension(240, 336);
    int TIMEOUT = 3;

    UUID createThumbnail(byte[] document);

    UUID createThumbnail(byte[] document, Integer side);

    boolean isDone(UUID id);

    @Cacheable(value = "forhaandsvisning")
    byte[] getThumbnail(UUID id);

    @Cacheable("forhaandsvisningId")
    UUID bestillForhaandsvisning(Dokument dokument);

    @Cacheable(value = "forhaandsvisningId")
    UUID bestillForhaandsvisning(Dokument dokument, Integer side, Dimension dimension);

    @Cacheable(value = "forhaandsvisning", key = "#id + \"-\" + #dimension.toString() + \"-\" + #side")
    byte[] hentForhaandsvisning(String id, Dimension dimension, Integer side, byte[] bilde);

    @Cacheable(value = "forhaandsvisning", key = "#id + \"-\" + #dimension.toString() + \"-\" + #side")
    byte[] hentForhaandsvisning(long id, Dimension dimension, int side);
}
