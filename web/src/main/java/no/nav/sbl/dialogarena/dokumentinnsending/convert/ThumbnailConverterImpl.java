package no.nav.sbl.dialogarena.dokumentinnsending.convert;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import no.nav.sbl.dialogarena.pdf.ConvertToPng;
import no.nav.sbl.dialogarena.pdf.ImageScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.AsyncTaskExecutor;

import javax.inject.Inject;
import java.awt.Dimension;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Klasse for å generere Thumbnail for et dokument. Godtar både bilder og pdf.
 * <p/>
 * Genereringen skjer asynkront. Eksempel på bruk:
 * <p/>
 * UUID id = ts.bestillForhaandsvisning(document);
 * while(!ts.isDone(id) {
 * Thread.Sleep(100);
 * }
 * byte[] thumbnail = ts.getThumbnail(id);
 */
public class ThumbnailConverterImpl implements ThumbnailConverter {
    public static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailConverterImpl.class);

    @Inject
    private AsyncTaskExecutor executor;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private SoknadService soknadService;


    /**
     * Starter generering av forhåndsvisning av et bilde. Returnerer en id som kan spørres på.
     *
     * @param document en pdf eller et bilde som et byte array.
     * @return id'en en kan spørre om svar på senere.
     */
    @Override
    public UUID createThumbnail(byte[] document) {
        return createThumbnail(document, 0);
    }

    /**
     * Starter generering av forhåndsvisning av et bilde med gitt dimensjon. Returnerer en id som kan spørres på.
     *
     * @param document en pdf eller et bilde som et byte array.
     * @param side     siden det skal genereres preview av
     * @return id'en en kan spørre om svar på senere.
     */
    @Override
    public UUID createThumbnail(final byte[] document, final Integer side) {
        final UUID uuid = UUID.randomUUID();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    setThumbnail(uuid, new ConvertToPng(SMALL, ImageScaler.ScaleMode.SCALE_TO_FIT_INSIDE_BOX, side).transform(document));
                } catch (RuntimeException ex) {
                    LOGGER.info("Feilet under generering av pdf med: {}", ex);
                    setThumbnail(uuid, new byte[0]);
                }
            }
        };
        executor.execute(task);
        leggTilId(uuid);
        return uuid;
    }

    /**
     * Check if the thumbnail is finished.
     *
     * @param id the id to check
     * @return true if the thumbnail is finished processing.
     */
    @Override
    public boolean isDone(UUID id) {
        kontrollerAtIdFinns(id);
        return cacheManager.getCache("forhaandsvisning").get(id) != null;
    }

    /**
     * Returnerer en forhåndsvisning av bildet. Gjøres som et blokkerende kall.
     *
     * @param id id til bildet. Kaster exception om id ikke finnes.
     * @return den ferdige thumbnail.
     * @throws RuntimeException om det har skjedd en feil under generering av bildet, eller om id'en ikke finnes.
     */
    @Override
    @Cacheable(value = "forhaandsvisning")
    public byte[] getThumbnail(UUID id) {
        kontrollerAtIdFinns(id);
        return null;
    }

    private void setThumbnail(UUID id, byte[] bilde) {
        cacheManager.getCache("forhaandsvisning").put(id, bilde);
    }

    private void leggTilId(UUID id) {
        cacheManager.getCache("forhaandsvisningId").put(id, id);
    }


    private void kontrollerAtIdFinns(UUID id) {
        if (cacheManager.getCache("forhaandsvisningId").get(id) == null) {
            throw new ApplicationException("UUID finnes ikke i systemet");
        }
    }

    /**
     * Gjør klar for å hente innhold fra web-service. Metoden er nå kun en skall metode.
     *
     * @param dokument dokumentet det skal hente
     * @return id det kan hentes ut bilde på
     */
    @Override
    @Cacheable("forhaandsvisningId")
    public UUID bestillForhaandsvisning(Dokument dokument) {
        return bestillForhaandsvisning(dokument, 0, LARGE);
    }

    /**
     * Gjør klar for å hente innhold fra web-service. Metoden er nå kun en skall metode.
     *
     * @param dokument dokumentet det skal hente
     * @param side     siden som det skal genereres preview av
     * @return id det kan hentes ut bilde på
     */
    @Override
    @Cacheable(value = "forhaandsvisningId")
    public UUID bestillForhaandsvisning(final Dokument dokument, final Integer side, final Dimension dimension) {
        final UUID uuid = UUID.randomUUID();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    setThumbnail(uuid, new ConvertToPng(dimension, ImageScaler.ScaleMode.SCALE_TO_FIT_INSIDE_BOX, side).transform(dokument.getDokumentInnhold().hentInnholdSomBytes()));
                } catch (RuntimeException ex) {
                    LOGGER.info("Feilet under generering av pdf med: " + ex, ex);
                    setThumbnail(uuid, new byte[0]);
                }
            }

        };
        leggTilId(uuid);
        executor.execute(task);
        return uuid;
    }

    @Override
    @Cacheable(value = "forhaandsvisning", key = "#id + \"-\" + #dimension.toString() + \"-\" + #side")
    public byte[] hentForhaandsvisning(final String id, final Dimension dimension, final Integer side, final byte[] bilde) {
        return submitAndWait(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return new ConvertToPng(dimension, ImageScaler.ScaleMode.SCALE_TO_FIT_INSIDE_BOX, side).transform(bilde);
            }
        });
    }

    @Override
    @Cacheable(value = "forhaandsvisning", key = "#id + \"-\" + #dimension.toString() + \"-\" + #side")
    public byte[] hentForhaandsvisning(final long id, final Dimension dimension, final int side) {
        final DokumentInnhold dokumentInnhold = soknadService.hentDokumentInnhold(id);
        return submitAndWait(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return hentForhaandsvisning(String.format("%d", id), dimension, side, dokumentInnhold.hentInnholdSomBytes());
            }
        });
    }

    private byte[] submitAndWait(Callable<byte[]> task) {
        try {
            return executor.submit(task).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ApplicationException("Kunne ikke lage forhaandsvisning: " + e, e);
        }
    }
}