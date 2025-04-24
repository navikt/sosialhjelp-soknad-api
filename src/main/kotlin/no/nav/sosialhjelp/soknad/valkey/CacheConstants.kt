package no.nav.sosialhjelp.soknad.valkey

const val KOMMUNEINFO_CACHE_KEY = "alle-kommuneinfo-key"
const val KOMMUNEINFO_LAST_POLL_TIME_KEY = "kommuneinfo-last-poll-time-key"
const val KOMMUNEINFO_CACHE_SECONDS = 10 * 60 * 60L // 10 timer

const val GT_CACHE_KEY_PREFIX = "GT-"
const val GT_LAST_POLL_TIME_PREFIX = "GT-last-poll-time-"

const val PERSON_CACHE_KEY_PREFIX = "hentperson-person-"
const val BARN_CACHE_KEY_PREFIX = "hentperson-barn-"
const val EKTEFELLE_CACHE_KEY_PREFIX = "hentperson-ektefelle-"
const val ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX = "hentperson-adressebeskyttelse-"
const val GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX = "hent-geografisktilknytning-"
const val PDL_CACHE_SECONDS = 30 * 60L // 30 minutter

const val KRR_CACHE_KEY_PREFIX = "krr-"
const val KONTOREGISTER_KONTONUMMER_CACHE_KEY_PREFIX = "kontoregister-kontonummer-"

const val CACHE_30_MINUTES_IN_SECONDS = 30 * 60L
const val CACHE_24_HOURS_IN_SECONDS = 24 * 60 * 60L
