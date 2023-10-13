package no.nav.sosialhjelp.soknad.model

// TODO Brukes alle?
/**
 * @see [no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype]
 */
enum class Botype {
    EIER,
    LEIER,
    KOMMUNAL,
    INGEN,
    INSTITUSJON,
    KRISESENTER,
    FENGSEL,
    VENNER,
    FORELDRE,
    FAMILIE,
    ANNET;

    companion object {
        fun fromValue(value: String?): Botype? = value?.let { Botype.valueOf(it) }
    }
}