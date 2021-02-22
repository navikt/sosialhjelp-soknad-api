package no.nav.sosialhjelp.soknad.consumer.redis;

public final class CacheConstants {

    private CacheConstants() {
    }

    public static final String KOMMUNEINFO_CACHE_KEY = "alle_kommuneinfo_key";
    public static final String KOMMUNEINFO_LAST_POLL_TIME_KEY = "kommuneinfo_last_poll_time_key";
    public static final long KOMMUNEINFO_CACHE_SECONDS = 10 * 60 * 60; // 10 timer

    public static final String KODEVERK_LAST_POLL_TIME_KEY = "kodeverk_last_poll_time_key";
    public static final String POSTNUMMER_CACHE_KEY = "postnummer_key";
    public static final String KOMMUNER_CACHE_KEY = "kommuner_key";
    public static final String LANDKODER_CACHE_KEY = "landkoder_key";
    public static final long KODEVERK_CACHE_SECONDS = 24 * 60 * 60; // 24 timer

    public static final String GT_CACHE_KEY_PREFIX = "GT_";
    public static final String GT_LAST_POLL_TIME_PREFIX = "GT_last_poll_time_";
    public static final long NORG_CACHE_SECONDS = 24 * 60 * 60; // 24 timer
}
