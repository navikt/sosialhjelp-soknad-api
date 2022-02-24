package no.nav.sosialhjelp.soknad.client.redis

const val KOMMUNEINFO_CACHE_KEY = "alle-kommuneinfo-key"
const val KOMMUNEINFO_LAST_POLL_TIME_KEY = "kommuneinfo-last-poll-time-key"
const val KOMMUNEINFO_CACHE_SECONDS = 10 * 60 * 60L // 10 timer

const val KODEVERK_LAST_POLL_TIME_KEY = "kodeverk-last-poll-time-key"
const val POSTNUMMER_CACHE_KEY = "postnummer-key"
const val KOMMUNER_CACHE_KEY = "kommuner-key"
const val LANDKODER_CACHE_KEY = "landkoder-key"
const val KODEVERK_CACHE_SECONDS = 24 * 60 * 60L // 24 timer

const val GT_CACHE_KEY_PREFIX = "GT-"
const val GT_LAST_POLL_TIME_PREFIX = "GT-last-poll-time-"

const val PERSON_CACHE_KEY_PREFIX = "hentperson-person-"
const val BARN_CACHE_KEY_PREFIX = "hentperson-barn-"
const val EKTEFELLE_CACHE_KEY_PREFIX = "hentperson-ektefelle-"
const val ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX = "hentperson-adressebeskyttelse-"
const val GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX = "hent-geografisktilknytning-"
const val PDL_CACHE_SECONDS = 30 * 60L // 30 minutter

const val KRR_CACHE_KEY_PREFIX = "krr-"
const val KONTONUMMER_CACHE_KEY_PREFIX = "kontonummer-"
const val NAVUTBETALINGER_CACHE_KEY_PREFIX = "navutbetalinger-"

const val TOKENDINGS_CACHE_KEY_PREFIX = "tokendings-"
const val AZURE_SYSTEM_TOKEN = "azure-"

const val CACHE_30_SECONDS = 30L
const val CACHE_30_MINUTES_IN_SECONDS = 30 * 60L
const val CACHE_24_HOURS_IN_SECONDS = 24 * 60 * 60L
