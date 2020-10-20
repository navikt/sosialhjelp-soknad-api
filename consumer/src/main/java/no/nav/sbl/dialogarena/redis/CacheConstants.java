package no.nav.sbl.dialogarena.redis;

public final class CacheConstants {

    private CacheConstants() {
    }

    public static final String KOMMUNEINFO_CACHE_KEY = "alle_kommuneinfo_key";
    public static final long KOMMUNEINFO_CACHE_SECONDS = 10 * 60 * 60; // 10 timer

    public static final String KOMMUNEINFO_LAST_POLL_TIME_KEY = "kommuneinfo_last_poll_time_key";
}
