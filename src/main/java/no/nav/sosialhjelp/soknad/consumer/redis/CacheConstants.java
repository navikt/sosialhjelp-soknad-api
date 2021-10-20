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
    public static final long KODEVERK_CACHE_SECONDS = 24 * 60 * 60L; // 24 timer

    public static final String GT_CACHE_KEY_PREFIX = "GT-";
    public static final String GT_LAST_POLL_TIME_PREFIX = "GT-last-poll-time-";
    public static final long NORG_CACHE_SECONDS = 24 * 60 * 60L; // 24 timer

    public static final String PERSON_CACHE_KEY_PREFIX = "hentperson-person-";
    public static final String BARN_CACHE_KEY_PREFIX = "hentperson-barn-";
    public static final String EKTEFELLE_CACHE_KEY_PREFIX = "hentperson-ektefelle-";
    public static final String ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX = "hentperson-adressebeskyttelse-";
    public static final String GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX = "hent-geografisktilknytning-";
    public static final long PDL_CACHE_SECONDS = 30 * 60L; // 30 minutter

    public static final String DKIF_CACHE_KEY_PREFIX = "dkif-";
    public static final String KONTONUMMER_CACHE_KEY_PREFIX = "kontonummer-";
    public static final String NAVUTBETALINGER_CACHE_KEY_PREFIX = "navutbetalinger-";

    public static final long CACHE_30_MINUTES_IN_SECONDS = 30 * 60L;
}
