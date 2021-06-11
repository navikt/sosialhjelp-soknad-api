package no.nav.sosialhjelp.soknad.consumer.redis;

public final class CacheConstants {

    private CacheConstants() {
    }

    public static final String KOMMUNEINFO_CACHE_KEY = "alle_kommuneinfo_key";
    public static final String KOMMUNEINFO_LAST_POLL_TIME_KEY = "kommuneinfo_last_poll_time_key";
    public static final long KOMMUNEINFO_CACHE_SECONDS = 10 * 60 * 60L; // 10 timer

    public static final String KODEVERK_LAST_POLL_TIME_KEY = "kodeverk_last_poll_time_key";
    public static final String POSTNUMMER_CACHE_KEY = "postnummer_key";
    public static final String KOMMUNER_CACHE_KEY = "kommuner_key";
    public static final String LANDKODER_CACHE_KEY = "landkoder_key";
    public static final long KODEVERK_CACHE_SECONDS = 24 * 60 * 60L; // 24 timer

    public static final String GT_CACHE_KEY_PREFIX = "GT_";
    public static final String GT_LAST_POLL_TIME_PREFIX = "GT_last_poll_time_";
    public static final long NORG_CACHE_SECONDS = 24 * 60 * 60L; // 24 timer

    public static final String PERSON_CACHE_KEY = "hentperson_person_";
    public static final String BARN_CACHE_KEY = "hentperson_barn_";
    public static final String EKTEFELLE_CACHE_KEY = "hentperson_ektefelle_";
    public static final String ADRESSEBESKYTTELSE_CACHE_KEY = "hentperson_adressebeskyttelse_";
    public static final long PDL_CACHE_SECONDS = 30 * 60L; // 30 minutter
}
