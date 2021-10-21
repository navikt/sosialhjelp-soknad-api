package no.nav.sosialhjelp.soknad.consumer.redis;

public final class CacheConstants {

    private CacheConstants() {
    }

    public static final String KOMMUNEINFO_CACHE_KEY = "alle-kommuneinfo-key";
    public static final String KOMMUNEINFO_LAST_POLL_TIME_KEY = "kommuneinfo-last-poll-time-key";
    public static final long KOMMUNEINFO_CACHE_SECONDS = 10 * 60 * 60L; // 10 timer

    public static final String KODEVERK_LAST_POLL_TIME_KEY = "kodeverk-last-poll-time-key";
    public static final String POSTNUMMER_CACHE_KEY = "postnummer-key";
    public static final String KOMMUNER_CACHE_KEY = "kommuner-key";
    public static final String LANDKODER_CACHE_KEY = "landkoder-key";

    public static final String GT_CACHE_KEY_PREFIX = "GT-";
    public static final String GT_LAST_POLL_TIME_PREFIX = "GT-last-poll-time-";

    public static final long CACHE_24_HOURS_IN_SECONDS = 24 * 60 * 60L;
    public static final long CACHE_30_MINUTES_IN_SECONDS = 30 * 60L;
}
